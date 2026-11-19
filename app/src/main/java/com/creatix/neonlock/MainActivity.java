package com.creatix.neonlock;

import android.app.Activity;
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
        add(content,text(this,"MOLETTE 3D • ÉDITION ORIGINALE",15,MUTED,false),this,8);
        preview=new ClockView(this,true);
        LinearLayout.LayoutParams frame=new LinearLayout.LayoutParams(-1,dp(this,340));
        frame.topMargin=dp(this,12);
        content.addView(preview,frame);

        Button wallpaper=button(this,"◉  ACTIVER LE FOND D'ÉCRAN ANIMÉ",GREEN,Color.BLACK);
        wallpaper.setOnClickListener(v->chooseWallpaper());
        add(content,wallpaper,this,12);
        Button customize=button(this,"⚙  PERSONNALISER LA MOLETTE 3D",PANEL,WHITE);
        customize.setOnClickListener(v->startActivity(new Intent(this,SettingsActivity.class)));
        add(content,customize,this,8);
        Button androidLock=button(this,"🔒  RÉGLAGES DU VERROUILLAGE ANDROID",PANEL,WHITE);
        androidLock.setOnClickListener(v->{
            try { startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS)); }
            catch(Exception error){
                Toast.makeText(this,"Ouvre les paramètres de sécurité Android.",Toast.LENGTH_LONG).show();
            }
        });
        add(content,androidLock,this,8);
        add(content,text(this,
            "La molette 3D utilise ton visuel original : elle tourne en 60 secondes. "+
            "Les chiffres 0 à 6 tournent sous le triangle orange, avec "+
            "la vraie heure et les secondes en temps réel au centre. "+
            "Le balayage et le verrouillage éventuel restent ceux d'Android ; "+
            "cette application n'y demande aucun accès.",13,MUTED,false),this,16);
        setContentView(scroll);
    }
    @Override protected void onResume(){
        super.onResume();
        if(preview!=null)preview.invalidate();
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
            VaultArt.draw(canvas,getWidth(),getHeight(),getContext(),preview);
        }
    }

    public static final class LiveWallpaper extends WallpaperService {
        @Override public Engine onCreateEngine(){return new NeonEngine();}
        private final class NeonEngine extends Engine {
            private final Handler timer=new Handler(Looper.getMainLooper());
            private final Runnable redraw=this::drawNow;
            private boolean visible=false;
            NeonEngine(){setTouchEventsEnabled(false);}
            @Override public void onVisibilityChanged(boolean shown){
                visible=shown;
                timer.removeCallbacks(redraw);
                if(shown)drawNow();
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
                    if(canvas!=null)VaultArt.draw(canvas,canvas.getWidth(),canvas.getHeight(),
                        LiveWallpaper.this,false);
                }catch(Exception ignored){
                    // A temporary wallpaper surface can disappear while turning off the screen.
                }finally{
                    if(canvas!=null){
                        try{getSurfaceHolder().unlockCanvasAndPost(canvas);}
                        catch(Exception ignored){}
                    }
                }
                if(visible)timer.postDelayed(redraw,33);
            }
        }
    }
}
