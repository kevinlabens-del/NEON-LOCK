package com.creatix.neonlock;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
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
        TextView title=text(this,"NEON CLOCK",32,WHITE,true);
        title.setLetterSpacing(.09f);
        add(content,title,this,12);
        add(content,text(this,"Fond animé • secondes 0 à 6 • heure centrale",15,MUTED,false),this,8);
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
            "La roue n'est plus un code : elle parcourt six segments de 10 secondes. "+
            "Le petit compteur indique les secondes restantes avant la minute suivante. "+
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
                timer.postDelayed(this,200);
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

    /** Six 10-second intervals: labels 0..6 along a 270-degree neon arc. */
    static final class ClockArt {
        private static final float START=135f;
        private static final float SWEEP=270f;
        private ClockArt(){}

        static void draw(Canvas canvas,int width,int height,Context context,boolean preview){
            if(width<=0||height<=0)return;
            canvas.drawColor(BG);
            LockPrefs.Style style=LockPrefs.get(context);
            float cx=width/2f;
            float cy=height*(preview?.51f:style.verticalPercent/100f);
            float r=Math.min(width*.36f,height*(preview?.37f:.20f));
            float density=context.getResources().getDisplayMetrics().density;
            r=Math.max(12f,r);
            Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setDither(true);
            p.setStyle(Paint.Style.FILL);
            p.setShader(new RadialGradient(cx,cy,r*2.15f,
                new int[]{Color.rgb(22,35,56),BG},null,Shader.TileMode.CLAMP));
            canvas.drawCircle(cx,cy,r*2.15f,p);
            p.setShader(null);

            RectF track=new RectF(cx-r,cy-r,cx+r,cy+r);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(density*5f);
            p.setColor(withAlpha(style.ringColor,55));
            canvas.drawArc(track,START,SWEEP,false,p);

            Calendar now=Calendar.getInstance();
            float seconds=now.get(Calendar.SECOND)+now.get(Calendar.MILLISECOND)/1000f;
            float traveled=SWEEP*(seconds/60f);
            p.setColor(withAlpha(style.ringColor,70));
            p.setStrokeWidth(density*10f);
            canvas.drawArc(track,START,traveled,false,p);
            p.setColor(style.ringColor);
            p.setStrokeWidth(density*3f);
            canvas.drawArc(track,START,traveled,false,p);

            for(int i=0;i<=60;i++){
                float angle=(float)Math.toRadians(START+SWEEP*i/60f);
                boolean major=i%10==0;
                float outer=r*(major?1.01f:.995f);
                float inner=r*(major?.91f:.962f);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(density*(major?1.8f:1f));
                p.setColor(withAlpha(style.numberColor,major?220:90));
                canvas.drawLine(cx+(float)Math.cos(angle)*inner,
                    cy+(float)Math.sin(angle)*inner,
                    cx+(float)Math.cos(angle)*outer,
                    cy+(float)Math.sin(angle)*outer,p);
            }
            p.setStyle(Paint.Style.FILL);
            p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
            p.setTextSize(Math.max(density*14,r*.145f));
            p.setTextAlign(Paint.Align.CENTER);
            for(int i=0;i<=6;i++){
                float angle=(float)Math.toRadians(START+SWEEP*i/6f);
                p.setColor(i==(int)(seconds/10)?style.ringColor:style.numberColor);
                canvas.drawText(String.valueOf(i),
                    cx+(float)Math.cos(angle)*r*.77f,
                    cy+(float)Math.sin(angle)*r*.77f-p.ascent()*.35f,
                    p);
            }

            float angle=(float)Math.toRadians(START+traveled);
            float px=cx+(float)Math.cos(angle)*r;
            float py=cy+(float)Math.sin(angle)*r;
            p.setColor(style.ringColor);
            canvas.drawCircle(px,py,density*5,p);
            p.setColor(withAlpha(style.ringColor,65));
            canvas.drawCircle(px,py,density*10,p);

            p.setColor(withAlpha(style.clockColor,35));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(density*1.4f);
            canvas.drawCircle(cx,cy,r*.47f,p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(style.clockColor);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
            p.setTextSize(Math.max(density*31,r*.30f));
            String time=String.format(Locale.getDefault(),"%02d:%02d",
                now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE));
            canvas.drawText(time,cx,cy-p.ascent()*.20f,p);

            p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
            p.setTextSize(Math.max(density*11,r*.105f));
            p.setColor(style.numberColor);
            int remaining=60-now.get(Calendar.SECOND);
            canvas.drawText(String.format(Locale.getDefault(),"%02d s restantes",remaining),
                cx,cy+r*.37f,p);

            if(style.showTitle){
                p.setTextSize(Math.max(density*15,r*.13f));
                p.setTypeface(Typeface.DEFAULT_BOLD);
                p.setColor(style.titleColor);
                canvas.drawText("NEON CLOCK",cx,cy-r*1.35f,p);
            }
            if(style.showDate){
                p.setTypeface(Typeface.DEFAULT);
                p.setTextSize(Math.max(density*12,r*.11f));
                p.setColor(style.dateColor);
                canvas.drawText(new SimpleDateFormat("EEE d MMM yyyy",
                    Locale.getDefault()).format(new Date()),cx,cy+r*1.37f,p);
            }
        }
        private static int withAlpha(int color,int alpha){
            return Color.argb(alpha,Color.red(color),Color.green(color),Color.blue(color));
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
                if(visible)timer.postDelayed(redraw,200);
            }
        }
    }
}
