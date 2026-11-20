package com.creatix.neonlock;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * On-device render smoke tests and performance diagnostics.
 * Runs without accessing logs, identifiers, accounts or remote analytics.
 */
public final class DiagnosticsActivity extends Activity {
    private TextView result;
    private String report="Aucun diagnostic exécuté.";
    private static final String EXPECTED_SIGNER=
        "96ef5146ed4c1d0a6531d316e40e2fab7354fcc02c5e2890ceb82fef24c4f1cc";

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(0xff030814);
        getWindow().setNavigationBarColor(0xff030814);
        ScrollView scroll=new ScrollView(this);
        scroll.setBackgroundColor(0xff050b17);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(19),dp(26),dp(19),dp(35));
        scroll.addView(root);

        add(root,text("NEON LOCK • DIAGNOSTICS",23,0xfff5fbff),0);
        add(root,text("Tests visuels des 12 thèmes, mémoire, qualité "+
            "de signature et mode économie.",13,0xff92a9c7),10);
        Button run=MainActivity.button(this,"▶  TESTER LES 12 THÈMES",
            0xff36e8c7,0xff06111c);
        run.setOnClickListener(v->runTests());
        add(root,run,18);
        result=text(report,12,0xffd1e6f8);
        result.setGravity(Gravity.START);
        result.setTextIsSelectable(true);
        add(root,result,14);
        Button share=MainActivity.button(this,"↑  PARTAGER LE RAPPORT",
            0xff192a40,0xffe7f5ff);
        share.setOnClickListener(v->{
            Intent send=new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT,report);
            startActivity(Intent.createChooser(send,"Partager le diagnostic"));
        });
        add(root,share,15);
        Button done=MainActivity.button(this,"TERMINÉ",0xff152639,0xfff5fbff);
        done.setOnClickListener(v->finish());
        add(root,done,12);
        setContentView(scroll);
    }
    private int dp(int v){return MainActivity.dp(this,v);}
    private void add(LinearLayout root,android.view.View item,int margin){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(margin);root.addView(item,lp);
    }
    private TextView text(String value,int size,int color){
        TextView label=MainActivity.text(this,value,size,color,false);
        label.setGravity(Gravity.START);
        return label;
    }
    private String signer(){
        try{
            PackageInfo info=getPackageManager().getPackageInfo(
                getPackageName(),PackageManager.GET_SIGNING_CERTIFICATES);
            if(info.signingInfo==null)return "Signature non disponible";
            Signature[] signatures=info.signingInfo.getApkContentsSigners();
            if(signatures==null||signatures.length==0)return "Signature absente";
            byte[] digest=MessageDigest.getInstance("SHA-256")
                .digest(signatures[0].toByteArray());
            StringBuilder hex=new StringBuilder();
            for(byte b:digest)hex.append(String.format(Locale.ROOT,"%02x",b&255));
            boolean match=EXPECTED_SIGNER.equalsIgnoreCase(hex.toString());
            return match?"Clé NEON permanente : VALIDÉE":
                "Clé de signature différente de la v1.2.4 (diagnostic)";
        }catch(Exception error){return "Signature : "+error.getClass().getSimpleName();}
    }

    private void runTests(){
        result.setText("Tests en cours dans la mémoire du téléphone…");
        new Thread(()->{
            StringBuilder data=new StringBuilder();
            try{
                PackageInfo info=getPackageManager().getPackageInfo(getPackageName(),0);
                data.append("NEON LOCK ").append(info.versionName).append('\n');
            }catch(Exception error){}
            data.append("Package : ").append(getPackageName()).append('\n');
            data.append(signer()).append("\n\n");
            ActivityManager am=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            boolean lowRam=am!=null && am.isLowRamDevice();
            PowerManager power=(PowerManager)getSystemService(Context.POWER_SERVICE);
            boolean savedPower=power!=null && power.isPowerSaveMode();
            data.append("Mémoire limitée : ").append(lowRam?"oui":"non").append('\n');
            data.append("Économie batterie Android : ")
                .append(savedPower?"active":"inactive").append('\n');
            data.append("FPS cible : ")
                .append(StudioPrefs.fps(this,savedPower)).append('\n');
            data.append("Mode animation : ")
                .append(StudioPrefs.MOTIONS[StudioPrefs.mode(this)]).append('\n');
            data.append("Transitions : ")
                .append(StudioPrefs.transition(this)?"activées":"désactivées")
                .append('\n');
            data.append("Décor personnel sur thème sélectionné : ")
                .append(StudioBackdrop.exists(this,ThemePrefs.selected(this))?
                    "oui":"non").append('\n');
            data.append("Parallaxe : ")
                .append(ThemePrefs.parallaxEnabled(this)?"activée":"désactivée").append('\n');
            long used=(Runtime.getRuntime().totalMemory()-
                Runtime.getRuntime().freeMemory())/1024L/1024L;
            data.append("Mémoire Java utilisée : ").append(used).append(" Mo\n\n");

            int pass=0;
            for(int theme=0;theme<ThemePrefs.COUNT;theme++){
                Bitmap bitmap=null;
                long time=android.os.SystemClock.uptimeMillis();
                String status="OK";
                try{
                    bitmap=Bitmap.createBitmap(360,700,Bitmap.Config.ARGB_8888);
                    Canvas canvas=new Canvas(bitmap);
                    ThemeArt.draw(canvas,360,700,this,true,theme);
                    // Additional path: valid live-wallpaper context with entrance.
                    ThemeArt.draw(canvas,360,700,this,false,theme,false,.75f,.4f);
                    pass++;
                }catch(Throwable error){
                    status="ÉCHEC : "+error.getClass().getSimpleName();
                }finally{if(bitmap!=null&&!bitmap.isRecycled())bitmap.recycle();}
                long duration=android.os.SystemClock.uptimeMillis()-time;
                data.append(ThemePrefs.name(theme)).append(" : ")
                    .append(status).append(" (").append(duration).append(" ms)\n");
            }
            data.append("\nBILAN : ").append(pass).append("/12 thèmes rendus\n");
            data.append("Ces tests ne remplacent pas une observation sur l'écran "+
                "de verrouillage ni une mesure de batterie réelle.");
            String output=data.toString();
            runOnUiThread(()->{
                report=output;
                if(result!=null)result.setText(report);
            });
        },"neon-smoke-tests").start();
    }
}
