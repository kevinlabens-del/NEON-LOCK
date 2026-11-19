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
     * NEON VAULT: heavy bank-vault door, beveled steel, fasteners, hinges and
     * a mechanical 0..6 combination dial moving at 1 revolution per minute.
     * Visual wallpaper only: never intercepts Android's genuine lock screen.
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
            p.setTypeface(Typeface.create(bold?"sans-serif-condensed":"sans-serif",
                bold?Typeface.BOLD:Typeface.NORMAL));
            c.drawText(s,x,y,p);
        }
        static int alpha(int rgb,int opacity){
            return Color.argb(opacity,Color.red(rgb),Color.green(rgb),Color.blue(rgb));
        }
        static float radius(float w,float h,boolean preview,float cy){
            float candidate=Math.min(w*.310f,h*(preview?.283f:.215f));
            float space=Math.min(cy-h*.06f,h-cy-h*.025f);
            return Math.max(1f,Math.min(candidate,space/1.66f));
        }
        static void outline(Canvas c,Paint p,float x,float y,float radius,
                            float line,int color){
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(line);p.setColor(color);
            c.drawCircle(x,y,radius,p);
            p.setStyle(Paint.Style.FILL);
        }
        static void screw(Canvas c,Paint p,float x,float y,float size,
                          int highlight,boolean large){
            p.setStyle(Paint.Style.FILL);
            p.setShader(new RadialGradient(x-size*.26f,y-size*.28f,size*1.5f,
                new int[]{Color.rgb(153,164,177),Color.rgb(63,77,96),
                    Color.rgb(15,24,38)},
                new float[]{0f,.44f,1f},Shader.TileMode.CLAMP));
            c.drawCircle(x,y,size,p);p.setShader(null);
            outline(c,p,x,y,size*.88f,size*.10f,Color.rgb(12,20,31));
            if(large){
                Path hex=new Path();
                for(int t=0;t<6;t++){
                    float a=(float)Math.toRadians(t*60f-30f);
                    float xx=x+(float)Math.cos(a)*size*.58f;
                    float yy=y+(float)Math.sin(a)*size*.58f;
                    if(t==0)hex.moveTo(xx,yy);else hex.lineTo(xx,yy);
                }
                hex.close();
                p.setColor(Color.rgb(41,54,69));p.setStyle(Paint.Style.FILL);
                c.drawPath(hex,p);
                p.setColor(highlight);p.setStrokeWidth(size*.10f);
                p.setStyle(Paint.Style.STROKE);c.drawPath(hex,p);
                p.setStyle(Paint.Style.FILL);
            }else{
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(size*.22f);
                p.setColor(Color.rgb(16,25,37));
                c.drawLine(x-size*.40f,y+size*.30f,x+size*.40f,y-size*.30f,p);
                p.setStyle(Paint.Style.FILL);
            }
        }
        static void hinge(Canvas c,Paint p,float x,float y,float r){
            float half=r*.116f,h=r*.23f;
            RectF base=new RectF(x-half*1.38f,y-h,x+half*1.38f,y+h);
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(base.left,0,base.right,0,
                new int[]{Color.rgb(15,25,39),Color.rgb(116,127,138),
                    Color.rgb(33,47,62),Color.rgb(8,14,27)},
                null,Shader.TileMode.CLAMP));
            c.drawRoundRect(base,r*.045f,r*.045f,p);
            p.setShader(null);
            p.setColor(Color.rgb(7,12,20));
            c.drawRoundRect(new RectF(x-half*.50f,y-h*1.27f,
                x+half*.58f,y+h*1.27f),r*.06f,r*.06f,p);
            p.setShader(new LinearGradient(x-half*.50f,0,x+half*.58f,0,
                new int[]{Color.rgb(25,37,54),Color.rgb(158,170,183),
                    Color.rgb(42,59,78)},null,Shader.TileMode.CLAMP));
            c.drawRoundRect(new RectF(x-half*.37f,y-h*1.20f,
                x+half*.43f,y+h*1.20f),r*.055f,r*.055f,p);
            p.setShader(null);
            screw(c,p,x,y-h*.74f,r*.038f,Color.rgb(107,125,142),false);
            screw(c,p,x,y+h*.74f,r*.038f,Color.rgb(107,125,142),false);
        }

        static void draw(Canvas canvas,int width,int height,Context context,boolean preview){
            if(width<=0||height<=0)return;
            final float w=width,h=height,cx=w*.5f;
            LockPrefs.Style style=LockPrefs.get(context);
            float cy=h*(preview?.51f:style.verticalPercent/100f);
            float r=radius(w,h,preview,cy);
            if(r<6f)return;
            int accent=style.ringColor;
            Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
            p.setDither(true);

            // Brushed black steel wall.
            p.setShader(new LinearGradient(0,0,w,h,
                new int[]{Color.rgb(3,6,12),Color.rgb(15,24,37),
                    Color.rgb(4,8,16),Color.rgb(9,15,25)},
                new float[]{0f,.40f,.67f,1f},Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,w,h,p);p.setShader(null);
            p.setStrokeWidth(Math.max(1f,r*.006f));
            for(int i=0;i<46;i++){
                float xx=(i+.5f)*w/46f;
                p.setColor(Color.argb(i%3==0?17:7,159,179,198));
                canvas.drawLine(xx,0,xx,h,p);
            }

            // Gigantic recessed steel door plate, NOT a simple circular clock.
            RectF shadow=new RectF(cx-r*1.48f,cy-r*1.68f,
                cx+r*1.48f,cy+r*1.68f);
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(0,3,8));
            canvas.drawRoundRect(shadow,r*.18f,r*.18f,p);
            RectF door=new RectF(cx-r*1.43f,cy-r*1.63f,
                cx+r*1.43f,cy+r*1.63f);
            p.setShader(new LinearGradient(door.left,door.top,door.right,door.bottom,
                new int[]{Color.rgb(15,24,38),Color.rgb(69,86,104),
                    Color.rgb(28,43,61),Color.rgb(12,20,34)},
                new float[]{0f,.32f,.66f,1f},Shader.TileMode.CLAMP));
            canvas.drawRoundRect(door,r*.16f,r*.16f,p);p.setShader(null);

            RectF inner=new RectF(cx-r*1.37f,cy-r*1.56f,
                cx+r*1.37f,cy+r*1.56f);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(r*.030f);p.setColor(Color.rgb(7,12,23));
            canvas.drawRoundRect(inner,r*.13f,r*.13f,p);
            p.setStrokeWidth(r*.010f);p.setColor(Color.rgb(128,147,166));
            canvas.drawRoundRect(new RectF(cx-r*1.325f,cy-r*1.505f,
                cx+r*1.325f,cy+r*1.505f),r*.10f,r*.10f,p);
            p.setStyle(Paint.Style.FILL);

            // Fine CNC horizontal scoring on the door's face.
            for(int i=-8;i<=8;i++){
                float yy=cy+i*r*.172f;
                p.setColor(i%2==0?Color.argb(19,188,205,221):
                    Color.argb(15,5,11,22));
                p.setStrokeWidth(Math.max(1f,r*.005f));
                canvas.drawLine(cx-r*1.27f,yy,cx+r*1.27f,yy,p);
            }

            // Left locking bars; two strong visible hinges on the right.
            for(int k=-1;k<=1;k+=2){
                float yy=cy+k*r*.73f;
                p.setColor(Color.rgb(8,15,26));
                canvas.drawRoundRect(new RectF(cx-r*1.40f,yy-r*.075f,
                    cx-r*.93f,yy+r*.075f),r*.025f,r*.025f,p);
                p.setShader(new LinearGradient(cx-r*1.4f,yy,cx-r*.93f,yy,
                    new int[]{Color.rgb(39,53,71),Color.rgb(149,164,179),
                        Color.rgb(31,44,62)},null,Shader.TileMode.CLAMP));
                canvas.drawRoundRect(new RectF(cx-r*1.37f,yy-r*.044f,
                    cx-r*.96f,yy+r*.044f),r*.018f,r*.018f,p);
                p.setShader(null);
                hinge(canvas,p,cx+r*1.38f,yy,r);
            }

            // Four corner studs and engraved bank-vault serial plates.
            for(int xx=-1;xx<=1;xx+=2){
                for(int yy=-1;yy<=1;yy+=2)
                    screw(canvas,p,cx+xx*r*1.21f,cy+yy*r*1.38f,
                        r*.075f,Color.rgb(131,145,161),true);
            }
            center(canvas,p,"V A U L T   /   0 6",cx,cy-r*1.39f,
                r*.113f,Color.rgb(183,194,205),true);
            center(canvas,p,"C R 3 @ T I X    S T E E L",cx,cy+r*1.47f,
                r*.087f,Color.rgb(139,155,169),true);

            // Forged door ring, with 12 hex head bolts.
            p.setShader(new RadialGradient(cx-r*.12f,cy-r*.13f,r*1.57f,
                new int[]{Color.rgb(122,137,151),Color.rgb(39,54,72),
                    Color.rgb(12,22,37),Color.rgb(4,9,18)},
                new float[]{0f,.55f,.84f,1f},Shader.TileMode.CLAMP));
            canvas.drawCircle(cx,cy,r*1.27f,p);p.setShader(null);
            outline(canvas,p,cx,cy,r*1.265f,r*.021f,Color.rgb(8,14,25));
            outline(canvas,p,cx,cy,r*1.23f,r*.010f,Color.rgb(162,178,190));
            outline(canvas,p,cx,cy,r*1.11f,r*.031f,Color.rgb(7,15,27));
            outline(canvas,p,cx,cy,r*1.08f,r*.012f,Color.rgb(137,154,171));
            for(int i=0;i<12;i++){
                double a=Math.toRadians(i*30f-90f);
                screw(canvas,p,cx+(float)Math.cos(a)*r*1.18f,
                    cy+(float)Math.sin(a)*r*1.18f,
                    r*.072f,Color.rgb(133,146,162),true);
            }

            // Rotating combination dial (it moves; door, hinges and bolts don't).
            Calendar now=Calendar.getInstance();
            float elapsed=now.get(Calendar.SECOND)+now.get(Calendar.MILLISECOND)/1000f;
            float phase=elapsed/60f;
            float rotation=360f/7f+360f*phase;
            int secondsLeft=60-now.get(Calendar.SECOND);
            int highlighted=(6-(Math.round(phase*7f)%7)+7)%7;

            p.setStyle(Paint.Style.FILL);
            p.setShader(new RadialGradient(cx-r*.21f,cy-r*.25f,r*1.35f,
                new int[]{Color.rgb(89,102,117),Color.rgb(42,57,73),
                    Color.rgb(18,29,45),Color.rgb(8,16,28)},
                new float[]{0f,.32f,.73f,1f},Shader.TileMode.CLAMP));
            canvas.drawCircle(cx,cy,r*.98f,p);p.setShader(null);
            outline(canvas,p,cx,cy,r*.972f,r*.024f,Color.rgb(141,156,171));
            outline(canvas,p,cx,cy,r*.935f,r*.010f,Color.rgb(5,13,25));

            // Concentric machining grooves, plus moving tick marks.
            for(int i=0;i<5;i++){
                outline(canvas,p,cx,cy,r*(.84f+i*.021f),r*.0034f,
                    i%2==0?Color.rgb(74,91,110):Color.rgb(12,25,41));
            }
            p.setStrokeCap(Paint.Cap.ROUND);
            for(int tick=0;tick<120;tick++){
                float a=(float)Math.toRadians(tick*3f-90f+rotation);
                boolean big=tick%10==0;
                p.setColor(big?Color.rgb(171,185,197):Color.rgb(92,110,129));
                p.setStrokeWidth(r*(big?.012f:.0045f));
                float inner=r*(big?.81f:.865f),outer=r*.918f;
                canvas.drawLine(cx+(float)Math.cos(a)*inner,
                    cy+(float)Math.sin(a)*inner,
                    cx+(float)Math.cos(a)*outer,
                    cy+(float)Math.sin(a)*outer,p);
            }

            // Seven inset mechanical number wells.
            for(int i=0;i<=6;i++){
                float a=(float)Math.toRadians(i*(360f/7f)-90f+rotation);
                float x=cx+(float)Math.cos(a)*r*.675f;
                float y=cy+(float)Math.sin(a)*r*.675f;
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.rgb(6,14,26));
                canvas.drawCircle(x,y,r*.142f,p);
                outline(canvas,p,x,y,r*.136f,r*.013f,
                    i==highlighted?accent:Color.rgb(101,117,133));
                center(canvas,p,Integer.toString(i),x,y+r*.050f,r*.159f,
                    i==highlighted?accent:style.numberColor,i==highlighted);
            }

            // Orange peripheral illuminated arc, under a stationary pointer.
            p.setStrokeCap(Paint.Cap.ROUND);p.setStyle(Paint.Style.STROKE);
            p.setColor(alpha(accent,45));p.setStrokeWidth(r*.10f);
            canvas.drawArc(cx-r*1.075f,cy-r*1.075f,
                cx+r*1.075f,cy+r*1.075f,194f,134f,false,p);
            p.setColor(accent);p.setStrokeWidth(r*.024f);
            canvas.drawArc(cx-r*1.075f,cy-r*1.075f,
                cx+r*1.075f,cy+r*1.075f,194f,134f,false,p);
            canvas.drawArc(cx-r*1.12f,cy-r*1.12f,
                cx+r*1.12f,cy+r*1.12f,12f,37f,false,p);
            p.setStyle(Paint.Style.FILL);

            // Triangle shaped index of a real safe combination dial.
            Path marker=new Path();
            marker.moveTo(cx-r*.095f,cy-r*1.043f);
            marker.lineTo(cx+r*.095f,cy-r*1.043f);
            marker.lineTo(cx,cy-r*.835f);
            marker.close();
            p.setColor(alpha(accent,90));
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.042f);
            canvas.drawPath(marker,p);
            p.setStyle(Paint.Style.FILL);p.setColor(accent);
            canvas.drawPath(marker,p);

            // A separate non-rotating steel spindle containing the live time.
            p.setShader(new RadialGradient(cx-r*.10f,cy-r*.12f,r*.53f,
                new int[]{Color.rgb(47,64,81),Color.rgb(8,16,28),Color.rgb(2,7,15)},
                new float[]{0f,.65f,1f},Shader.TileMode.CLAMP));
            canvas.drawCircle(cx,cy,r*.386f,p);p.setShader(null);
            outline(canvas,p,cx,cy,r*.386f,r*.022f,Color.rgb(131,151,168));
            outline(canvas,p,cx,cy,r*.349f,r*.006f,alpha(accent,130));
            String time=String.format(Locale.getDefault(),"%02d:%02d",
                now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE));
            center(canvas,p,time,cx,cy+r*.065f,r*.295f,style.clockColor,true);
            center(canvas,p,String.format(Locale.getDefault(),"%02d SEC",secondsLeft),
                cx,cy+r*.255f,r*.106f,accent,true);

            if(style.showTitle)
                center(canvas,p,"NEON  LOCK",cx,h*.11f,r*.17f,style.titleColor,true);
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
