package com.creatix.neonlock;

import android.app.Activity;
import android.app.DownloadManager;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.PowerManager;
import android.provider.Settings;
import android.service.wallpaper.WallpaperService;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * NEON LOCK is now a decorative seconds clock wallpaper, NOT a lock screen.
 * Android owns all unlocking, security and the native swipe gesture.
 * No AccessibilityService, overlay, visual PIN or Keyguard manipulation.
 */
public final class MainActivity extends Activity {
    static final int BG=Color.rgb(6,9,17);
    static final int PANEL=Color.rgb(19,25,39);
    static final int ORANGE=Color.rgb(255,171,43);
    static final int GREEN=Color.rgb(43,255,179);
    static final int WHITE=Color.rgb(232,241,254);
    static final int MUTED=Color.rgb(142,159,184);
    private ClockView preview;
    private Button pendingInstaller;
    private boolean receiverRegistered=false;
    private final BroadcastReceiver downloads=new BroadcastReceiver(){
        @Override public void onReceive(Context context,Intent intent){
            if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())){
                NeonUpdates.checkDownloaded(MainActivity.this,false);
                if(pendingInstaller!=null)pendingInstaller.setVisibility(View.VISIBLE);
            }
        }
    };

    static int dp(Context context,float amount){
        return Math.round(context.getResources().getDisplayMetrics().density*amount);
    }
    static TextView text(Context c,String value,int size,int color,boolean bold){
        TextView view=new TextView(c);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER_HORIZONTAL);
        if(bold)view.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return view;
    }
    static Button button(Context c,String label,int background,int foreground){
        Button b=new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTextColor(foreground);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(background));
        b.setMinHeight(dp(c,54));
        return b;
    }
    static void add(LinearLayout group,View item,Context c,int margin){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(c,margin);
        group.addView(item,lp);
    }

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        // Purge legacy visual-lock credentials if an older version was installed.
        getSharedPreferences("neon_lock_visual",MODE_PRIVATE).edit().clear().apply();
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        ScrollView scroll=new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(this,22),dp(this,32),dp(this,22),dp(this,32));
        scroll.addView(content);
        TextView mark=text(this,"CR3@TIX / ANDROID",12,GREEN,true);
        mark.setLetterSpacing(.2f);
        add(content,mark,this,0);
        TextView title=text(this,"NEON LOCK",32,WHITE,true);
        title.setLetterSpacing(.09f);
        add(content,title,this,12);
        add(content,text(this,"12 THÈMES 3D  •  ÉDITION PREMIUM",15,MUTED,false),this,8);
        preview=new ClockView(this,true);
        LinearLayout.LayoutParams frame=new LinearLayout.LayoutParams(-1,dp(this,340));
        frame.topMargin=dp(this,12);
        content.addView(preview,frame);

        Button wallpaper=button(this,"◉  ACTIVER LE FOND D'ÉCRAN ANIMÉ",GREEN,Color.BLACK);
        wallpaper.setOnClickListener(v->chooseWallpaper());
        add(content,wallpaper,this,12);
        Button galleryButton=button(this,"▦  GALERIE DES 12 THÈMES",PANEL,WHITE);
        galleryButton.setOnClickListener(v->startActivity(new Intent(this,ThemeGalleryActivity.class)));
        add(content,galleryButton,this,8);
        Button customize=button(this,"⚙  PERSONNALISER MON THÈME",PANEL,WHITE);
        customize.setOnClickListener(v->startActivity(new Intent(this,ThemeSettingsActivity.class)));
        add(content,customize,this,8);
        Button updates=button(this,"↻  VÉRIFIER LES MISES À JOUR",PANEL,WHITE);
        updates.setOnClickListener(v->NeonUpdates.check(this,true));
        add(content,updates,this,8);
        pendingInstaller=button(this,"⬇  INSTALLER LA MISE À JOUR TÉLÉCHARGÉE",PANEL,WHITE);
        pendingInstaller.setOnClickListener(v->NeonUpdates.checkDownloaded(this,true));
        pendingInstaller.setVisibility(NeonUpdates.hasPending(this)?View.VISIBLE:View.GONE);
        add(content,pendingInstaller,this,8);
        if(Build.VERSION.SDK_INT>=33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){
            Button notifications=button(this,"🔔  ACTIVER LES ALERTES DE MISE À JOUR",PANEL,WHITE);
            notifications.setOnClickListener(v->requestPermissions(
                new String[]{Manifest.permission.POST_NOTIFICATIONS},2401));
            add(content,notifications,this,8);
        }
        add(content,text(this,
            "Choisis parmi 12 ambiances 3D : Vault Original, Cyber, Royal Gold, "+
            "Red Reactor, Steampunk, Galaxy Pulse, Arc Reactor, Neon Grid, Frost Core, Lava Core, Shadow Tech et Ocean Pulse. "+
            "L'heure reste au format HH:mm et tes réglages sont sauvegardés. "+
            "NEON LOCK vérifie automatiquement les nouvelles versions officielles. "+
            "Si une mise à jour existe, tu peux la télécharger puis confirmer "+
            "son installation avec Android. Le fond animé et ses réglages "+
            "sont conservés lorsque les APK ont la même signature.",13,MUTED,false),this,16);
        setContentView(scroll);
        NeonUpdates.schedule(this);
    }
    @Override protected void onResume(){
        super.onResume();
        if(preview!=null)preview.invalidate();
        if(pendingInstaller!=null)
            pendingInstaller.setVisibility(NeonUpdates.hasPending(this)?View.VISIBLE:View.GONE);
        NeonUpdates.checkDownloaded(this,false);
        NeonUpdates.check(this,false);
    }

    @Override protected void onStart(){
        super.onStart();
        try{
            IntentFilter completed=new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            if(Build.VERSION.SDK_INT>=33)
                registerReceiver(downloads,completed,Context.RECEIVER_EXPORTED);
            else registerReceiver(downloads,completed);
            receiverRegistered=true;
        }catch(Exception ignored){receiverRegistered=false;}
    }

    @Override protected void onStop(){
        if(receiverRegistered){
            try{unregisterReceiver(downloads);}catch(Exception ignored){}
            receiverRegistered=false;
        }
        super.onStop();
    }
    private void chooseWallpaper(){
        try {
            Intent intent=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this,LiveWallpaper.class));
            startActivity(intent);
        }catch(Exception error){
            try { startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)); }
            catch(Exception ignored){
                Toast.makeText(this,"Sélecteur de fond animé indisponible.",Toast.LENGTH_LONG).show();
            }
        }
    }

    public static final class ClockView extends View {
        private final Handler timer=new Handler(Looper.getMainLooper());
        private boolean attached=false;
        private final Runnable update=new Runnable(){
            @Override public void run(){
                if(!attached)return;
                invalidate();
                timer.postDelayed(this,33);
            }
        };
        private final boolean preview;
        ClockView(Context context,boolean isPreview){super(context);preview=isPreview;}
        @Override protected void onAttachedToWindow(){
            super.onAttachedToWindow();
            attached=true;
            timer.removeCallbacks(update);
            timer.post(update);
        }
        @Override protected void onDetachedFromWindow(){
            attached=false;
            timer.removeCallbacks(update);
            super.onDetachedFromWindow();
        }
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            ThemeArt.draw(canvas,getWidth(),getHeight(),getContext(),preview);
        }
    }

    public static final class LiveWallpaper extends WallpaperService {
        @Override public Engine onCreateEngine(){return new NeonEngine();}
        private final class NeonEngine extends Engine {
            private final Handler timer=new Handler(Looper.getMainLooper());
            private final Runnable redraw=this::drawNow;
            private boolean visible=false;
            private float xOffset=.5f;
            private long appearedAt=0L;
            private int lastTheme=-1;
            private int fromTheme=-1;
            private long themeChangedAt=0L;
            NeonEngine(){
                setTouchEventsEnabled(false);
                setOffsetNotificationsEnabled(true);
            }
            @Override public void onVisibilityChanged(boolean shown){
                visible=shown;
                timer.removeCallbacks(redraw);
                if(shown){
                    appearedAt=SystemClock.uptimeMillis();
                    drawNow();
                }
            }
            @Override public void onOffsetsChanged(float x,float y,
                float xStep,float yStep,int pixelX,int pixelY){
                xOffset=ThemeMath.clamp(x,0f,1f);
                if(visible && ThemePrefs.parallaxEnabled(LiveWallpaper.this))
                    drawNow();
            }
            @Override public void onWallpaperFlagsChanged(int which){
                if(android.os.Build.VERSION.SDK_INT>=34 && visible)drawNow();
            }
            private boolean lockOnly(){
                if(android.os.Build.VERSION.SDK_INT<34)return false;
                int flags=getWallpaperFlags();
                return (flags & WallpaperManager.FLAG_LOCK)!=0 &&
                    (flags & WallpaperManager.FLAG_SYSTEM)==0;
            }
            @Override public void onSurfaceChanged(SurfaceHolder holder,
                    int format,int width,int height){
                super.onSurfaceChanged(holder,format,width,height);
                if(visible)drawNow();
            }
            @Override public void onSurfaceDestroyed(SurfaceHolder holder){
                timer.removeCallbacks(redraw);
                super.onSurfaceDestroyed(holder);
            }
            @Override public void onDestroy(){
                visible=false;
                timer.removeCallbacks(redraw);
                super.onDestroy();
            }
            private void drawNow(){
                timer.removeCallbacks(redraw);
                if(!visible)return;
                Canvas canvas=null;
                try {
                    canvas=getSurfaceHolder().lockCanvas();
                    if(canvas!=null){
                        float entrance=ThemePrefs.startupEnabled(LiveWallpaper.this)?
                            ThemeMath.clamp((SystemClock.uptimeMillis()-appearedAt)/1650f,
                                0f,1f):1f;
                        int theme=ThemePrefs.active(LiveWallpaper.this);
                        long now=SystemClock.uptimeMillis();
                        if(lastTheme>=0 && lastTheme!=theme){
                            fromTheme=lastTheme;
                            themeChangedAt=now;
                        }
                        lastTheme=theme;
                        float progress=StudioMath.fadeProgress(
                            now,themeChangedAt,750L);
                        if(StudioPrefs.transition(LiveWallpaper.this) &&
                            fromTheme>=0 && progress<1f){
                            // Crossfade the old and new wallpaper using one
                            // offscreen alpha layer (only for 750 ms).
                            ThemeArt.draw(canvas,canvas.getWidth(),canvas.getHeight(),
                                LiveWallpaper.this,false,fromTheme,lockOnly(),
                                xOffset,1f);
                            int save=canvas.saveLayerAlpha(0f,0f,
                                canvas.getWidth(),canvas.getHeight(),
                                Math.round(progress*255f));
                            ThemeArt.draw(canvas,canvas.getWidth(),canvas.getHeight(),
                                LiveWallpaper.this,false,theme,lockOnly(),
                                xOffset,entrance);
                            canvas.restoreToCount(save);
                        }else{
                            fromTheme=-1;
                            ThemeArt.draw(canvas,canvas.getWidth(),canvas.getHeight(),
                                LiveWallpaper.this,false,theme,lockOnly(),
                                xOffset,entrance);
                        }
                    }
                }catch(Exception ignored){
                    // A temporary wallpaper surface can disappear while turning off the screen.
                }finally{
                    if(canvas!=null){
                        try{getSurfaceHolder().unlockCanvasAndPost(canvas);}
                        catch(Exception ignored){}
                    }
                }
                if(visible){
                    PowerManager power=(PowerManager)getSystemService(POWER_SERVICE);
                    boolean lowPower=power!=null && power.isPowerSaveMode();
                    int fps=StudioPrefs.fps(LiveWallpaper.this,lowPower);
                    timer.postDelayed(redraw,Math.max(24,1000/fps));
                }
            }
        }
    }
}
