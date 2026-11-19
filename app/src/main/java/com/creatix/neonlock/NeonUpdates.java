package com.creatix.neonlock;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.JobInfo;
import android.app.JobScheduler;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Updates are checked against official NEON-LOCK GitHub Releases, not a third-party server.
 * The APK digest and signing certificate are verified before presenting Android's installer.
 *
 * No silent installs: the user must approve the Android install/unknown-source prompts.
 * Builds MUST retain the same private signing key for in-place upgrades.
 */
final class NeonUpdates {
    static final String API="https://api.github.com/repos/kevinlabens-del/NEON-LOCK/releases/latest";
    static final String CHANNEL="neon_updates";
    private static final String PREFS="neon_update_state";
    private static final String ID="download_id";
    private static final String SHA="download_sha";
    private static final String VERSION="download_version";
    private static final String URL="download_url";
    private static final long CHECK_INTERVAL=6L*60L*60L*1000L;
    private static final int JOB_ID=45124;
    private static final ExecutorService IO=Executors.newSingleThreadExecutor();
    private static boolean checking=false;
    private static boolean prompting=false;
    private NeonUpdates(){}

    static final class Release {
        final String name,url,digest;
        Release(String name,String url,String digest){
            this.name=name;this.url=url;this.digest=digest;
        }
    }

    static void schedule(Context context){
        try{
            JobScheduler scheduler=(JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if(scheduler==null)return;
            JobInfo job=new JobInfo.Builder(JOB_ID,
                new ComponentName(context,UpdateCheckJob.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(12L*60L*60L*1000L)
                .build();
            scheduler.schedule(job);
            NotificationManager nm=(NotificationManager)context.getSystemService(
                Context.NOTIFICATION_SERVICE);
            if(nm!=null)nm.createNotificationChannel(new NotificationChannel(
                CHANNEL,"Mises à jour NEON LOCK",NotificationManager.IMPORTANCE_DEFAULT));
        }catch(Exception ignored){
            // OEM background-job limits must never break the wallpaper.
        }
    }

    static void check(Activity activity,boolean manual){
        long now=System.currentTimeMillis();
        android.content.SharedPreferences prefs=activity.getSharedPreferences(PREFS,0);
        if(!manual && now-prefs.getLong("last_checked",0)<CHECK_INTERVAL)return;
        synchronized(NeonUpdates.class){
            if(checking){
                if(manual)Toast.makeText(activity,"Vérification déjà en cours.",Toast.LENGTH_SHORT).show();
                return;
            }
            checking=true;
        }
        if(manual)Toast.makeText(activity,"Recherche de mises à jour…",Toast.LENGTH_SHORT).show();
        IO.execute(()->{
            Release release=null;
            String error=null;
            try{release=queryLatest(activity.getApplicationContext());}catch(Exception e){error=e.getMessage();}
            synchronized(NeonUpdates.class){checking=false;}
            final Release next=release;
            final String failure=error;
            activity.runOnUiThread(()->{
                if(activity.isFinishing() || activity.isDestroyed())return;
                if(next!=null){
                    prefs.edit().putLong("last_checked",System.currentTimeMillis()).apply();
                    showRelease(activity,next);
                }else if(manual){
                    if(failure!=null) new AlertDialog.Builder(activity)
                        .setTitle("Mise à jour indisponible")
                        .setMessage("Impossible de joindre GitHub pour le moment. Vérifie ta connexion Internet.\n"+
                            failure)
                        .setPositiveButton("OK",null).show();
                    else new AlertDialog.Builder(activity)
                        .setTitle("NEON LOCK est à jour")
                        .setMessage("Aucune version plus récente n'est publiée sur GitHub.")
                        .setPositiveButton("OK",null).show();
                }else if(failure==null){
                    prefs.edit().putLong("last_checked",System.currentTimeMillis()).apply();
                }
            });
        });
    }

    static void checkSilently(Context context,Runnable complete){
        IO.execute(()->{
            try{
                Release next=queryLatest(context);
                if(next!=null)notifyUpdate(context,next);
            }catch(Exception ignored){}finally{
                if(complete!=null)complete.run();
            }
        });
    }

    private static Release queryLatest(Context c) throws Exception{
        HttpURLConnection connection=null;
        try{
            connection=(HttpURLConnection)new URL(API).openConnection();
            connection.setConnectTimeout(9000);
            connection.setReadTimeout(9000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent","NEON-LOCK-Android-Updater");
            connection.setRequestProperty("Accept","application/vnd.github+json");
            if(connection.getResponseCode()!=200)
                throw new Exception("GitHub HTTP "+connection.getResponseCode());
            ByteArrayOutputStream data=new ByteArrayOutputStream(4096);
            try(InputStream in=connection.getInputStream()){
                byte[] buffer=new byte[8192];int n;
                while((n=in.read(buffer))!=-1){
                    if(data.size()+n>512000)throw new Exception("Réponse trop volumineuse");
                    data.write(buffer,0,n);
                }
            }
            JSONObject json=new JSONObject(data.toString("UTF-8"));
            String tag=json.optString("tag_name","");
            String installed=c.getPackageManager().getPackageInfo(c.getPackageName(),0).versionName;
            if(compareVersions(tag,installed)<=0)return null;
            JSONArray assets=json.optJSONArray("assets");
            if(assets==null)throw new Exception("Aucun fichier APK publié");
            for(int i=0;i<assets.length();i++){
                JSONObject a=assets.optJSONObject(i);
                if(a==null)continue;
                String name=a.optString("name","");
                String url=a.optString("browser_download_url","");
                String digest=a.optString("digest","");
                if(!name.equals("NEON-LOCK-"+tag+".apk"))continue;
                if(!url.startsWith("https://github.com/kevinlabens-del/NEON-LOCK/releases/download/"))
                    throw new Exception("Source APK non officielle");
                if(!digest.matches("sha256:[0-9a-fA-F]{64}"))
                    throw new Exception("Empreinte SHA-256 manquante");
                return new Release(tag,url,digest.substring(7).toLowerCase(Locale.ROOT));
            }
            throw new Exception("Fichier NEON-LOCK introuvable");
        }finally{if(connection!=null)connection.disconnect();}
    }

    private static int compareVersions(String incoming,String current){
        try{
            if(incoming.startsWith("v"))incoming=incoming.substring(1);
            if(current.startsWith("v"))current=current.substring(1);
            String[] a=incoming.split("\\."),b=current.split("\\.");
            for(int i=0;i<3;i++){
                int x=i<a.length?Integer.parseInt(a[i]):0;
                int y=i<b.length?Integer.parseInt(b[i]):0;
                if(x!=y)return Integer.compare(x,y);
            }
        }catch(Exception ignored){}
        return 0;
    }

    private static void showRelease(Activity activity,Release release){
        if(prompting)return;
        prompting=true;
        new AlertDialog.Builder(activity)
            .setTitle("NEON LOCK "+release.name+" disponible")
            .setMessage("Une mise à jour officielle est disponible. "+
                "L'application peut télécharger automatiquement l'APK, vérifier son "+
                "intégrité et ouvrir l'installation Android. "+
                "Une confirmation peut être demandée par ton téléphone.")
            .setPositiveButton("Télécharger",(d,w)->download(activity,release))
            .setNegativeButton("Plus tard",null)
            .setOnDismissListener(d->prompting=false)
            .show();
    }

    static void download(Activity activity,Release release){
        try{
            DownloadManager manager=(DownloadManager)activity.getSystemService(
                Context.DOWNLOAD_SERVICE);
            if(manager==null)throw new Exception("Téléchargements Android indisponibles");
            DownloadManager.Request request=new DownloadManager.Request(Uri.parse(release.url));
            request.setMimeType("application/vnd.android.package-archive");
            request.setTitle("NEON LOCK "+release.name);
            request.setDescription("Mise à jour officielle depuis GitHub");
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedOverMetered(true);
            request.setDestinationInExternalFilesDir(activity,Environment.DIRECTORY_DOWNLOADS,
                "NEON-LOCK-"+release.name+"-"+System.currentTimeMillis()+".apk");
            long id=manager.enqueue(request);
            activity.getSharedPreferences(PREFS,0).edit()
                .putLong(ID,id).putString(SHA,release.digest)
                .putString(VERSION,release.name).putString(URL,release.url)
                .remove("prompted").apply();
            Toast.makeText(activity,"Téléchargement lancé. Ouvre NEON LOCK une fois terminé "+
                "pour installer la mise à jour.",Toast.LENGTH_LONG).show();
        }catch(Exception error){
            new AlertDialog.Builder(activity)
                .setTitle("Téléchargement impossible")
                .setMessage(error.getMessage())
                .setPositiveButton("OK",null).show();
        }
    }

    static boolean hasPending(Context context){
        return context.getSharedPreferences(PREFS,0).getLong(ID,-1)>=0;
    }

    static void checkDownloaded(Activity activity,boolean manual){
        android.content.SharedPreferences prefs=activity.getSharedPreferences(PREFS,0);
        long id=prefs.getLong(ID,-1);
        if(id<0){
            if(manual)Toast.makeText(activity,"Aucune mise à jour téléchargée.",Toast.LENGTH_SHORT).show();
            return;
        }
        DownloadManager dm=(DownloadManager)activity.getSystemService(Context.DOWNLOAD_SERVICE);
        if(dm==null)return;
        int status=DownloadManager.STATUS_PENDING;
        try(Cursor cursor=dm.query(new DownloadManager.Query().setFilterById(id))){
            if(cursor==null || !cursor.moveToFirst())return;
            status=cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
        }catch(Exception e){return;}
        if(status==DownloadManager.STATUS_FAILED){
            prefs.edit().remove(ID).apply();
            if(manual)new AlertDialog.Builder(activity)
                .setTitle("Téléchargement échoué")
                .setMessage("Tu peux relancer la mise à jour depuis l'application.")
                .setPositiveButton("OK",null).show();
            return;
        }
        if(status!=DownloadManager.STATUS_SUCCESSFUL){
            if(manual)Toast.makeText(activity,"Téléchargement en cours…",Toast.LENGTH_SHORT).show();
            return;
        }
        String sha=prefs.getString(SHA,"");
        String version=prefs.getString(VERSION,"");
        if(!sha.matches("[0-9a-fA-F]{64}"))return;
        if(!manual && prefs.getLong("prompted",-1)==id)return;
        IO.execute(()->{
            String error=null;
            try{
                File target=UpdateApkProvider.getApkFile(activity);
                File parent=target.getParentFile();
                if(parent==null || (!parent.exists() && !parent.mkdirs()))
                    throw new Exception("Espace de stockage non disponible");
                MessageDigest sha256=MessageDigest.getInstance("SHA-256");
                try(ParcelFileDescriptor fd=dm.openDownloadedFile(id);
                    FileInputStream in=new FileInputStream(fd.getFileDescriptor());
                    FileOutputStream out=new FileOutputStream(target,false)){
                    byte[] buffer=new byte[32768];int n;
                    while((n=in.read(buffer))!=-1){
                        sha256.update(buffer,0,n);
                        out.write(buffer,0,n);
                    }
                    out.getFD().sync();
                }
                StringBuilder hex=new StringBuilder();
                for(byte byteValue:sha256.digest())
                    hex.append(String.format(Locale.ROOT,"%02x",byteValue&255));
                if(!sha.equalsIgnoreCase(hex.toString())){
                    target.delete();
                    throw new Exception("Empreinte SHA-256 invalide : installation annulée");
                }
                PackageManager pm=activity.getPackageManager();
                PackageInfo incoming=pm.getPackageArchiveInfo(target.getAbsolutePath(),
                    PackageManager.GET_SIGNING_CERTIFICATES);
                PackageInfo installed=pm.getPackageInfo(activity.getPackageName(),
                    PackageManager.GET_SIGNING_CERTIFICATES);
                if(incoming==null || !activity.getPackageName().equals(incoming.packageName))
                    throw new Exception("APK ou identifiant de l'application invalide");
                if(incoming.getLongVersionCode()<=installed.getLongVersionCode()){
                    prefs.edit().remove(ID).apply();
                    throw new Exception("Ce fichier n'est pas une nouvelle version");
                }
                Signature[] current=installed.signingInfo==null?null:
                    installed.signingInfo.getApkContentsSigners();
                Signature[] next=incoming.signingInfo==null?null:
                    incoming.signingInfo.getApkContentsSigners();
                if(current==null||next==null||!Arrays.equals(current,next)){
                    throw new Exception("La signature de cette APK ne correspond pas à celle "+
                        "déjà installée. Android refuse une mise à jour directe dans ce cas. "+
                        "Il faut une clé de signature conservée entre les versions.");
                }
            }catch(Exception ex){error=ex.getMessage();}
            String failure=error;
            activity.runOnUiThread(()->{
                if(activity.isFinishing()||activity.isDestroyed())return;
                if(failure!=null){
                    prefs.edit().putLong("prompted",id).apply();
                    new AlertDialog.Builder(activity)
                        .setTitle("Mise à jour non installable")
                        .setMessage(failure)
                        .setPositiveButton("OK",null).show();
                    return;
                }
                if(prompting)return;
                prompting=true;
                prefs.edit().putLong("prompted",id).apply();
                new AlertDialog.Builder(activity)
                    .setTitle("Installer NEON LOCK "+version+" ?")
                    .setMessage("Fichier téléchargé et vérifié. "+
                        "Le système Android demandera ta confirmation d'installation.")
                    .setPositiveButton("Installer",(dialog,which)->install(activity))
                    .setNegativeButton("Plus tard",null)
                    .setOnDismissListener(d->prompting=false)
                    .show();
            });
        });
    }

    private static void install(Activity activity){
        if(!activity.getPackageManager().canRequestPackageInstalls()){
            Uri source=Uri.parse("package:"+activity.getPackageName());
            Intent settings=new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,source);
            try{activity.startActivity(settings);}
            catch(Exception e){
                Toast.makeText(activity,"Autorise l'installation de sources inconnues pour "+
                    "NEON LOCK dans les paramètres Android.",Toast.LENGTH_LONG).show();
            }
            return;
        }
        try{
            Uri uri=UpdateApkProvider.getApkUri(activity);
            Intent view=new Intent(Intent.ACTION_VIEW);
            view.setDataAndType(uri,"application/vnd.android.package-archive");
            view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(view);
        }catch(Exception error){
            new AlertDialog.Builder(activity)
                .setTitle("Installation indisponible")
                .setMessage("Impossible d'ouvrir l'installateur Android : "+error.getMessage())
                .setPositiveButton("OK",null).show();
        }
    }

    private static void notifyUpdate(Context context,Release release){
        try{
            NotificationManager nm=(NotificationManager)context.getSystemService(
                Context.NOTIFICATION_SERVICE);
            if(nm==null)return;
            nm.createNotificationChannel(new NotificationChannel(CHANNEL,
                "Mises à jour NEON LOCK",NotificationManager.IMPORTANCE_DEFAULT));
            if(Build.VERSION.SDK_INT>=33 && context.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
                return;
            Intent open=new Intent(context,MainActivity.class);
            open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pending=PendingIntent.getActivity(context,454,open,
                PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notif=new Notification.Builder(context,CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("NEON LOCK "+release.name+" disponible")
                .setContentText("Ouvre l'application pour installer la nouvelle version")
                .setAutoCancel(true)
                .setContentIntent(pending)
                .build();
            nm.notify(454,notif);
        }catch(Exception ignored){}
    }
}
