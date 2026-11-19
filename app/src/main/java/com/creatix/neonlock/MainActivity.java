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
        add(content,text(this,"Cadran coffre-fort animé • roue 0 à 6 • heure centrale",15,MUTED,false),this,8);
        preview=new ClockView(this,true);
        LinearLayout.LayoutParams frame=new LinearLayout.LayoutParams(-1,dp(this,340));
        frame.topMargin=dp(this,12);
        content.addView(preview,frame);

        Button wallpaper=button(this,"◉  ACTIVER LE FOND D'ÉCRAN ANIMÉ",GREEN,Color.BLACK);
        wallpaper.setOnClickListener(v->chooseWallpaper());
        add(content,wallpaper,this,12);
        Button customize=button(this,"⚙  PERSONNALISER L'HORLOGE",PANEL,WHITE);
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
            "La roue de coffre-fort effectue un tour toutes les 60 secondes. "+
            "Les chiffres 0 à 6 tournent sous la flèche orange et un compteur "+
            "affiche les secondes restantes avant la minute suivante. "+
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
            ClockArt.draw(canvas,getWidth(),getHeight(),getContext(),preview);
        }
    }

    /**
     * Horloge conçue à partir du cadran original v0.5 :
     * fond bleu-noir, disque métallique, deux arcs orange, triangle fixe en haut.
     * La roue de chiffres 0..6 tourne continûment pendant une minute.
     * Le décompte précis 60..01 est affiché sous l'heure au centre.
     */
    static final class ClockArt {
        private ClockArt(){}

        static void center(Canvas c,Paint p,String s,float x,float y,float size,
                           int color,boolean bold){
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(size);
            p.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));
            c.drawText(s,x,y,p);
        }

        private static int tint(int rgb,int alpha){
            return Color.argb(alpha,Color.red(rgb),Color.green(rgb),Color.blue(rgb));
        }

        private static float radius(float w,float h,boolean preview,float cy){
            float base=Math.min(w*.39f,h*(preview?.29f:.23f));
            float bottomFree=h-cy-h*(preview?.06f:.07f);
            return Math.max(1f,Math.min(base,bottomFree/(preview?1.33f:1.15f)));
        }

        static void draw(Canvas canvas,int width,int height,Context context,boolean preview){
            if(width<=0||height<=0)return;
            float w=width,h=height;
            LockPrefs.Style style=LockPrefs.get(context);
            Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
            p.setDither(true);

            // Même fond sombre satiné que la v0.5.
            p.setShader(new LinearGradient(0,0,w,h,
                new int[]{Color.rgb(3,6,12),Color.rgb(14,20,34),Color.rgb(4,8,16)},
                null,Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,w,h,p);
            p.setShader(null);

            float cx=w*.5f;
            float cy=h*(preview?.52f:style.verticalPercent/100f);
            float r=radius(w,h,preview,cy);
            if(r<5f)return;
            final int accent=style.ringColor;

            // Halo doux et disque métallisé.
            p.setShader(new RadialGradient(cx,cy,r*1.7f,
                new int[]{Color.argb(100,39,63,93),Color.argb(28,20,30,47),Color.TRANSPARENT},
                null,Shader.TileMode.CLAMP));
            canvas.drawCircle(cx,cy,r*1.7f,p);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(5,10,20));
            canvas.drawCircle(cx,cy,r*1.10f,p);
            p.setShader(new RadialGradient(cx-r*.18f,cy-r*.20f,r*1.26f,
                new int[]{Color.rgb(43,51,68),Color.rgb(24,32,49),
                    Color.rgb(10,15,27),Color.rgb(4,8,16)},
                new float[]{0f,.35f,.76f,1f},Shader.TileMode.CLAMP));
            canvas.drawCircle(cx,cy,r*.99f,p);
            p.setShader(null);

            // Biseaux concentriques d'une molette de coffre-fort.
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(r*.020f);
            p.setColor(Color.rgb(62,75,95));
            canvas.drawCircle(cx,cy,r*.985f,p);
            p.setColor(Color.rgb(14,24,42));
            p.setStrokeWidth(r*.037f);
            canvas.drawCircle(cx,cy,r*.955f,p);
            p.setStrokeWidth(r*.010f);
            p.setColor(Color.rgb(87,104,125));
            canvas.drawCircle(cx,cy,r*.930f,p);

            // Les arcs orange caractéristiques du modèle 0.5, restent fixes.
            p.setStrokeWidth(r*.052f);
            p.setColor(tint(accent,62));
            canvas.drawArc(cx-r*1.075f,cy-r*1.075f,cx+r*1.075f,cy+r*1.075f,
                194f,134f,false,p);
            p.setColor(accent);
            p.setStrokeWidth(r*.022f);
            canvas.drawArc(cx-r*1.075f,cy-r*1.075f,cx+r*1.075f,cy+r*1.075f,
                194f,134f,false,p);
            canvas.drawArc(cx-r*1.12f,cy-r*1.12f,cx+r*1.12f,cy+r*1.12f,
                12f,37f,false,p);

            Calendar now=Calendar.getInstance();
            float elapsed=now.get(Calendar.SECOND)+now.get(Calendar.MILLISECOND)/1000f;
            int secondsLeft=60-now.get(Calendar.SECOND);
            float rotation=-360f*elapsed/60f;
            // 0 .. 6 : sept positions sur le disque de coffre-fort (1 tour/min).
            int highlighted=Math.round(elapsed/60f*7f)%7;

            // Rayures radiales de métal et 120 petites graduations qui tournent.
            for(int tick=0;tick<120;tick++){
                float angle=(float)Math.toRadians(tick*3f-90f+rotation);
                boolean heavy=tick%10==0;
                p.setColor(heavy?Color.rgb(126,151,172):Color.rgb(49,64,85));
                p.setStrokeWidth(r*(heavy?.012f:.0045f));
                float inner=r*(heavy?.80f:.86f);
                float outer=r*.91f;
                canvas.drawLine(cx+(float)Math.cos(angle)*inner,
                    cy+(float)Math.sin(angle)*inner,
                    cx+(float)Math.cos(angle)*outer,
                    cy+(float)Math.sin(angle)*outer,p);
            }

            // Chiffres sur la roue en mouvement, repère triangulaire IMMOBILE.
            for(int i=0;i<=6;i++){
                float a=(float)Math.toRadians(i*(360f/7f)-90f+rotation);
                float x=cx+(float)Math.cos(a)*r*.66f;
                float y=cy+(float)Math.sin(a)*r*.66f;
                boolean selected=(i==highlighted);
                center(canvas,p,String.valueOf(i),x,y+r*.052f,
                    r*(selected?.175f:.154f),
                    selected?accent:style.numberColor,selected);
            }

            Path arrow=new Path();
            arrow.moveTo(cx-r*.081f,cy-r*1.005f);
            arrow.lineTo(cx+r*.081f,cy-r*1.005f);
            arrow.lineTo(cx,cy-r*.865f);
            arrow.close();
            p.setColor(tint(accent,95));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(r*.033f);
            canvas.drawPath(arrow,p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(accent);
            canvas.drawPath(arrow,p);

            // Moyeu central, à la manière du cadran du coffre-fort v0.5.
            p.setColor(Color.rgb(7,11,20));
            canvas.drawCircle(cx,cy,r*.375f,p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(r*.014f);
            p.setColor(Color.rgb(75,89,110));
            canvas.drawCircle(cx,cy,r*.375f,p);
            p.setStyle(Paint.Style.FILL);
            String hour=String.format(Locale.getDefault(),"%02d:%02d",
                now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE));
            center(canvas,p,hour,cx,cy+r*.095f,r*.285f,style.clockColor,true);
            center(canvas,p,String.format(Locale.getDefault(),"%02d s",secondsLeft),
                cx,cy+r*.275f,r*.125f,accent,true);

            if(style.showTitle){
                center(canvas,p,"NEON  LOCK",cx,h*.11f,r*.17f,
                    style.titleColor,true);
            }
            if(style.showDate){
                String date=new SimpleDateFormat("EEEE d MMMM",Locale.FRANCE)
                    .format(new Date()).toUpperCase(Locale.FRANCE);
                center(canvas,p,date,cx,h*.15f,r*.105f,style.dateColor,false);
            }
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
                    if(canvas!=null)ClockArt.draw(canvas,canvas.getWidth(),canvas.getHeight(),
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
