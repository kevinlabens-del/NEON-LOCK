package com.creatix.neonlock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Twelve original, resolution-independent animated 3D wallpaper scenes.
 * The first scene delegates to the original photo-based Vault renderer.
 * Other themes are procedural drawings; no remote artwork or internet needed.
 */
final class ThemeArt {
    private ThemeArt(){}
    static void draw(Canvas c,int w,int h,Context context,boolean preview){
        draw(c,w,h,context,preview,ThemePrefs.active(context));
    }
    static void draw(Canvas c,int w,int h,Context context,boolean preview,int id){
        draw(c,w,h,context,preview,id,false,.5f,1f);
    }
    /**
     * Wallpaper-specific context: lock and home can use different positions,
     * xOffset provides touch-free home-screen parallax and entrance is 0..1.
     */
    static void draw(Canvas c,int w,int h,Context context,boolean preview,
                     int id,boolean lockScreen,float xOffset,float entrance){
        if(w<=0||h<=0)return;
        id=ThemePrefs.safe(id);
        entrance=ThemeMath.clamp(entrance,0f,1f);
        float entry=ThemeMath.easeOut(entrance);
        if(id==ThemePrefs.VAULT){
            c.save();
            // Original picture is never replaced or stretched into other themes.
            VaultArt.draw(c,w,h,context,preview,
                ThemePrefs.verticalForScreen(context,id,lockScreen)/100f,xOffset);
            if(entrance<1f)drawEntryPulse(c,w,h,context,id,entry);
            c.restore();
            return;
        }
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        p.setDither(true);
        float x=w*.5f;
        float y=h*(preview?.51f:
            ThemePrefs.verticalForScreen(context,id,lockScreen)/100f);
        float scale=ThemePrefs.size(context,id)/100f;
        float r=Math.min(w*.45f,h*(preview?.405f:.255f))*scale;
        r=Math.min(r,Math.min(y*.94f,(h-y)*.92f));
        if(r<8f)return;
        int accent=ThemePrefs.effectiveAccent(context,id);
        float glow=ThemePrefs.glowFactor(context,id);
        float time=(System.currentTimeMillis()%60000L)/1000f;
        float spin=time*6f*(ThemePrefs.reverse(context,id)?-1f:1f)
            *ThemePrefs.speed(context,id)/100f;
        int dark=0xff040915;
        if(id==ThemePrefs.GOLD)dark=0xff0a0807;
        if(id==ThemePrefs.STEAMPUNK)dark=0xff100b09;
        if(id==ThemePrefs.RED)dark=0xff09040b;
        if(id==ThemePrefs.GALAXY)dark=0xff0b061a;
        if(id==ThemePrefs.ARC)dark=0xff050c18;
        if(id==ThemePrefs.NEON_GRID)dark=0xff081025;
        if(id==ThemePrefs.FROST)dark=0xff0c223b;
        if(id==ThemePrefs.LAVA)dark=0xff240908;
        if(id==ThemePrefs.SHADOW)dark=0xff070d12;
        if(id==ThemePrefs.OCEAN)dark=0xff041d2b;

        drawBackground(c,p,w,h,x,y,r,dark,accent,glow,id,xOffset);
        float tick=System.currentTimeMillis()/1000f;
        if(id>=ThemePrefs.CYBER && id<=ThemePrefs.ARC)
            SceneArt.scenery(c,p,w,h,x,y,r,id,accent,glow,tick,
                ThemePrefs.parallaxEnabled(context)?xOffset:.5f);
        c.save();
        if(entry<1f)c.scale(.925f+.075f*entry,.925f+.075f*entry,x,y);
        // The original six skins retain their high-definition metal atlas;
        // five new ones use full-screen compositions, not compulsory round dials.
        if(id>=ThemePrefs.NEON_GRID){
            SceneArt.newScene(c,p,w,h,x,y,r,id,accent,glow,
                spin,tick,ThemePrefs.parallaxEnabled(context)?xOffset:.5f,
                ThemePrefs.digitStyle(context,id));
        }else{
            boolean textured=ThemeTexture.draw(c,p,context,x,y,r,id,
                accent,spin,glow,ThemePrefs.parallaxEnabled(context)?xOffset:.5f,
                tick);
            if(!textured){
                switch(id){
                    case ThemePrefs.CYBER: cyber(c,p,x,y,r,accent,spin,glow);break;
                    case ThemePrefs.GOLD: gold(c,p,x,y,r,accent,spin,glow);break;
                    case ThemePrefs.RED: red(c,p,x,y,r,accent,spin,glow);break;
                    case ThemePrefs.STEAMPUNK: steampunk(c,p,x,y,r,accent,spin,glow);break;
                    case ThemePrefs.GALAXY: galaxy(c,p,x,y,r,accent,spin,glow);break;
                    case ThemePrefs.ARC: arcReactor(c,p,x,y,r,accent,spin,glow);break;
                    default:cyber(c,p,x,y,r,accent,spin,glow);
                }
            }
        }
        // Always last: HH:mm only, independent of photo and moving digits.
        drawClock(c,p,x,y,r,id,accent,glow,
            ThemePrefs.centerStyle(context,id));
        if(entry<1f){
            float start=360f*entry-90f;
            arcGlow(c,p,x,y,r*.88f,r*.007f,start,30f,
                accent,(1f-entry)*glow);
        }
        c.restore();
        if(!preview){
            LockPrefs.Style user=LockPrefs.get(context);
            if(user.showTitle)
                lettering(c,p,"NEON LOCK",x,y-r*1.32f,r*.145f,user.titleColor,true);
            if(user.showDate){
                String date=new SimpleDateFormat("EEE d MMMM yyyy",Locale.FRANCE)
                    .format(new Date()).toUpperCase(Locale.FRANCE);
                lettering(c,p,date,x,y+r*1.28f,r*.10f,user.dateColor,false);
            }
            if(user.showCountdown){
                float bottom=y+r*1.16f;
                if(bottom<h-r*.08f){
                    int remaining=60-Calendar.getInstance().get(Calendar.SECOND);
                    lettering(c,p,String.format(Locale.FRANCE,"%02d SEC RESTANTES",remaining),
                        x,bottom,r*.073f,accent,false);
                }
            }
        }
    }

    private static int mix(int a,int b,float t){
        t=Math.max(0f,Math.min(1f,t));
        return Color.argb(255,
            (int)(Color.red(a)*(1-t)+Color.red(b)*t),
            (int)(Color.green(a)*(1-t)+Color.green(b)*t),
            (int)(Color.blue(a)*(1-t)+Color.blue(b)*t));
    }
    private static int alpha(int color,float fraction){
        return Color.argb(Math.max(0,Math.min(255,Math.round(fraction*255))),
            Color.red(color),Color.green(color),Color.blue(color));
    }
    private static void fillCircle(Canvas c,Paint p,float x,float y,float radius,int color){
        p.clearShadowLayer();p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);
        c.drawCircle(x,y,radius,p);
    }
    private static void stroke(Canvas c,Paint p,float x,float y,float radius,
                               float width,int color){
        p.setShader(null);p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(width);p.setColor(color);p.clearShadowLayer();
        c.drawCircle(x,y,radius,p);p.setStyle(Paint.Style.FILL);
    }
    private static void glowing(Canvas c,Paint p,float x,float y,float radius,
                                float width,int color,float intensity){
        p.setShader(null);p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(width*1.8f);
        p.setColor(alpha(color,.22f*intensity));
        p.setShadowLayer(width*2.0f,0f,0f,alpha(color,.85f));
        c.drawCircle(x,y,radius,p);
        p.clearShadowLayer();p.setStrokeWidth(width);
        p.setColor(mix(color,Color.WHITE,.20f));
        c.drawCircle(x,y,radius,p);
        p.setStyle(Paint.Style.FILL);
    }
    private static void arcGlow(Canvas c,Paint p,float x,float y,float radius,
                                float width,float start,float degrees,int color,float glow){
        p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(width*2.8f);p.setColor(alpha(color,.14f*glow));
        RectF bounds=new RectF(x-radius,y-radius,x+radius,y+radius);
        c.drawArc(bounds,start,degrees,false,p);
        p.setStrokeWidth(width);p.setColor(alpha(color,Math.min(1f,.7f*glow)));
        c.drawArc(bounds,start,degrees,false,p);
        p.setStyle(Paint.Style.FILL);
    }
    private static void metal(Canvas c,Paint p,float x,float y,float r,int accent,
                              boolean brass){
        int low=brass?0xff27190c:0xff121c29;
        int mid=brass?0xffad7537:0xff879aaa;
        int high=brass?0xfff8d89d:0xffe6f3ff;
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(x-r*.32f,y-r*.43f,r*1.65f,
            new int[]{mid,low,brass?0xff442b16:0xff213649,0xff04070e},
            new float[]{0f,.54f,.84f,1f},Shader.TileMode.CLAMP));
        c.drawCircle(x,y,r,p);p.setShader(null);

        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.064f);
        p.setShader(new SweepGradient(x,y,
            new int[]{low,high,mid,low,mid,high,low},
            new float[]{0f,.12f,.27f,.43f,.62f,.85f,1f}));
        c.drawCircle(x,y,r*.93f,p);p.setShader(null);
        stroke(c,p,x,y,r*.902f,r*.008f,high);
        stroke(c,p,x,y,r*.958f,r*.016f,low);
        stroke(c,p,x,y,r*.85f,r*.014f,brass?0xff5b3819:0xff182333);
        p.setStyle(Paint.Style.FILL);
    }
    private static void drawBackground(Canvas c,Paint p,int w,int h,float x,float y,
                                       float r,int dark,int accent,float glow,int id,
                                       float offset){
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0,0,w,h,new int[]{
            mix(dark,accent,.12f),dark,mix(dark,Color.BLACK,.50f),mix(dark,accent,.06f)
        },null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);p.setShader(null);
        p.setShader(new RadialGradient(x,y,r*2.05f,new int[]{
            alpha(accent,.21f*glow),alpha(accent,.065f*glow),Color.TRANSPARENT
        },new float[]{0f,.43f,1f},Shader.TileMode.CLAMP));
        c.drawCircle(x,y,r*2.05f,p);p.setShader(null);
        // Sparse cinematic light flecks, deterministic and stable.
        for(int i=0;i<22;i++){
            float a=(i*37.53f+id*29.5f);
            float px=(float)((Math.sin(a*.88)*.5+.5)*w)+
                ThemeMath.parallax(offset,r*.25f);
            float py=(float)((Math.cos(a*1.27)*.5+.5)*h);
            fillCircle(c,p,px,py,Math.max(.7f,r*.003f*(i%3+1)),
                alpha(i%3==0?accent:Color.WHITE,.12f+(i%4)*.07f));
        }
    }
    private static void ticks(Canvas c,Paint p,float x,float y,float r,int count,
                              float inner,float outer,int color,float rotate){
        p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);
        for(int i=0;i<count;i++){
            float a=(float)(i*2*Math.PI/count+Math.toRadians(rotate));
            float ri=r*inner,ro=r*(i%5==0?outer:inner+(outer-inner)*.50f);
            float s=(float)Math.sin(a),co=(float)Math.cos(a);
            p.setColor(i%5==0?color:alpha(color,.40f));
            p.setStrokeWidth(r*(i%5==0?.010f:.0048f));
            c.drawLine(x+co*ri,y+s*ri,x+co*ro,y+s*ro,p);
        }
        p.setStyle(Paint.Style.FILL);
    }
    private static void dot(Canvas c,Paint p,float x,float y,float radius,int color,float glow){
        p.clearShadowLayer();p.setStyle(Paint.Style.FILL);
        p.setColor(alpha(color,.20f*glow));
        c.drawCircle(x,y,radius*2.45f,p);
        p.setColor(color);c.drawCircle(x,y,radius,p);
    }
    private static void drawDigits(Canvas c,Paint p,float x,float y,float r,int accent,
                                   float spin,boolean roman){
        String[] romanNumbers={"XII","II","IV","VI","VIII","X"};
        p.setShader(null);p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create(roman?"serif":"sans-serif-condensed",Typeface.BOLD));
        p.setTextSize(r*(roman?.125f:.16f));
        p.setStyle(Paint.Style.FILL);
        Paint.FontMetrics fm=p.getFontMetrics();
        int n=roman?6:7;
        for(int i=0;i<n;i++){
            float angle=(float)Math.toRadians(-90.0+i*360.0/n+spin);
            float px=x+(float)Math.cos(angle)*r*.66f;
            float py=y+(float)Math.sin(angle)*r*.66f-(fm.ascent+fm.descent)*.5f;
            p.setColor(mix(accent,Color.WHITE,.63f));
            p.setShadowLayer(r*.025f,0,0,accent);
            c.drawText(roman?romanNumbers[i]:Integer.toString(i),px,py,p);
            p.clearShadowLayer();
        }
    }
    private static void centerDisc(Canvas c,Paint p,float x,float y,float r,
                                   int accent,boolean transparent){
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(x-r*.14f,y-r*.22f,r*.66f,
            new int[]{transparent?0xff192537:0xff182431,0xff070b14,0xff02040a},
            new float[]{0f,.66f,1f},Shader.TileMode.CLAMP));
        c.drawCircle(x,y,r*.43f,p);p.setShader(null);
        stroke(c,p,x,y,r*.43f,r*.013f,mix(accent,Color.WHITE,.15f));
        stroke(c,p,x,y,r*.39f,r*.007f,alpha(accent,.48f));
        p.setStyle(Paint.Style.FILL);
    }
    private static void cyber(Canvas c,Paint p,float x,float y,float r,int accent,
                              float spin,float glow){
        metal(c,p,x,y,r,accent,false);
        fillCircle(c,p,x,y,r*.845f,0xff061829);
        glowing(c,p,x,y,r*.825f,r*.018f,accent,glow);
        stroke(c,p,x,y,r*.76f,r*.010f,alpha(accent,.65f));
        ticks(c,p,x,y,r,84,.745f,.803f,accent,0f);
        for(int i=0;i<4;i++){
            float angle=spin*(i%2==0?1f:-.65f)+i*90f;
            arcGlow(c,p,x,y,r*(.73f-i*.075f),r*.011f,angle,75f-i*9,accent,glow);
        }
        for(int i=0;i<12;i++){
            float angle=(float)(i*2*Math.PI/12);
            dot(c,p,x+(float)Math.cos(angle)*r*.91f,
                y+(float)Math.sin(angle)*r*.91f,r*.012f,accent,glow);
        }
        drawDigits(c,p,x,y,r,accent,spin*.22f,false);
        centerDisc(c,p,x,y,r,accent,false);
        // Holographic concentric target and digital arcs.
        stroke(c,p,x,y,r*.31f,r*.003f,alpha(accent,.45f));
        arcGlow(c,p,x,y,r*.32f,r*.009f,-spin*.65f,95f,accent,glow);
        p.setStyle(Paint.Style.STROKE);p.setColor(alpha(accent,.28f));
        p.setStrokeWidth(r*.004f);
        c.drawLine(x-r*.25f,y,x+r*.25f,y,p);
        c.drawLine(x,y-r*.25f,x,y+r*.25f,p);
        p.setStyle(Paint.Style.FILL);
    }
    private static void gold(Canvas c,Paint p,float x,float y,float r,int accent,
                             float spin,float glow){
        metal(c,p,x,y,r,accent,true);
        fillCircle(c,p,x,y,r*.844f,0xff17110a);
        for(int i=0;i<3;i++){
            float rr=r*(.81f-i*.10f);
            stroke(c,p,x,y,rr,r*.012f,alpha(accent,.66f-i*.11f));
        }
        ticks(c,p,x,y,r,60,.72f,.79f,accent,0f);
        for(int i=0;i<12;i++){
            double a=Math.PI*2*i/12d;
            float ex=x+(float)Math.cos(a)*r*.90f;
            float ey=y+(float)Math.sin(a)*r*.90f;
            dot(c,p,ex,ey,r*.015f,mix(accent,Color.WHITE,.44f),glow);
        }
        drawDigits(c,p,x,y,r,accent,spin*.10f,true);
        gear(c,p,x-r*.28f,y-r*.24f,r*.21f,12,spin*.8f,accent,true);
        gear(c,p,x+r*.30f,y+r*.10f,r*.17f,10,-spin,accent,true);
        centerDisc(c,p,x,y,r,accent,true);
        stroke(c,p,x,y,r*.26f,r*.014f,alpha(accent,.62f));
        arcGlow(c,p,x,y,r*.31f,r*.009f,spin*.32f,94f,accent,glow);
        // Crown jewel at twelve o'clock.
        dot(c,p,x,y-r*.92f,r*.023f,0xffffe9a3,glow);
    }
    private static void red(Canvas c,Paint p,float x,float y,float r,int accent,
                            float spin,float glow){
        metal(c,p,x,y,r,accent,false);
        fillCircle(c,p,x,y,r*.845f,0xff19060b);
        stroke(c,p,x,y,r*.805f,r*.01f,alpha(accent,.70f));
        ticks(c,p,x,y,r,72,.735f,.82f,accent,spin*.18f);
        for(int i=0;i<12;i++){
            float angle=i*30f+spin*.30f;
            arcGlow(c,p,x,y,r*.80f,r*.045f,angle,13.0f,accent,glow);
        }
        for(int i=0;i<4;i++){
            arcGlow(c,p,x,y,r*(.65f-i*.09f),
                r*(i==2?.019f:.010f),-spin*(i%2==0?1f:-.55f)+i*72f,
                108f-i*9f,accent,glow);
        }
        drawDigits(c,p,x,y,r,accent,spin*.3f,false);
        centerDisc(c,p,x,y,r,accent,false);
        for(int i=0;i<8;i++){
            float a=(float)(i*Math.PI/4+Math.toRadians(spin*.3f));
            dot(c,p,x+(float)Math.cos(a)*r*.35f,
                y+(float)Math.sin(a)*r*.35f,r*.012f,accent,glow);
        }
    }
    private static void steampunk(Canvas c,Paint p,float x,float y,float r,int accent,
                                  float spin,float glow){
        metal(c,p,x,y,r,accent,true);
        fillCircle(c,p,x,y,r*.84f,0xff25160d);
        gear(c,p,x-r*.37f,y-r*.27f,r*.34f,15,spin*.38f,accent,true);
        gear(c,p,x+r*.39f,y-r*.14f,r*.29f,14,-spin*.44f,accent,true);
        gear(c,p,x+r*.29f,y+r*.38f,r*.22f,12,spin*.72f,accent,true);
        gear(c,p,x-r*.35f,y+r*.43f,r*.19f,11,-spin*.8f,accent,true);
        stroke(c,p,x,y,r*.80f,r*.025f,0xffd39a56);
        stroke(c,p,x,y,r*.74f,r*.012f,alpha(accent,.75f));
        ticks(c,p,x,y,r,60,.73f,.80f,accent,spin*.18f);
        // Copper rivets on the stationary outer framework.
        for(int i=0;i<8;i++){
            double a=i*Math.PI/4;
            float nx=x+(float)Math.cos(a)*r*.91f;
            float ny=y+(float)Math.sin(a)*r*.91f;
            fillCircle(c,p,nx,ny,r*.031f,0xff15110e);
            fillCircle(c,p,nx-r*.006f,ny-r*.006f,r*.019f,0xffe1ab65);
        }
        drawDigits(c,p,x,y,r,accent,spin*.12f,true);
        centerDisc(c,p,x,y,r,accent,false);
        stroke(c,p,x,y,r*.30f,r*.014f,alpha(accent,.65f));
        arcGlow(c,p,x,y,r*.32f,r*.008f,spin*.6f,74f,accent,glow);
    }
    private static void gear(Canvas c,Paint p,float x,float y,float radius,int teeth,
                             float rotation,int color,boolean brass){
        c.save();c.rotate(rotation,x,y);
        Path body=new Path();
        int n=teeth*4;
        for(int i=0;i<n;i++){
            double a=(i*2*Math.PI/n)-Math.PI/2;
            float k=(i%4==1||i%4==2)?1f:.86f;
            float xx=x+(float)Math.cos(a)*radius*k;
            float yy=y+(float)Math.sin(a)*radius*k;
            if(i==0)body.moveTo(xx,yy);else body.lineTo(xx,yy);
        }
        body.close();
        p.setShader(new RadialGradient(x-radius*.3f,y-radius*.4f,radius*1.6f,
            new int[]{brass?0xffc89052:color,brass?0xff5d351c:0xff162439,0xff100c0a},
            null,Shader.TileMode.CLAMP));
        p.setStyle(Paint.Style.FILL);c.drawPath(body,p);p.setShader(null);
        stroke(c,p,x,y,radius*.60f,radius*.12f,brass?0xffd49a56:color);
        fillCircle(c,p,x,y,radius*.28f,0xff09070b);
        stroke(c,p,x,y,radius*.22f,radius*.06f,color);
        c.restore();
    }
    private static void galaxy(Canvas c,Paint p,float x,float y,float r,int accent,
                               float spin,float glow){
        // Stable pinprick stars appear to orbit the deep-space ring.
        for(int i=0;i<95;i++){
            double a=i*2.39996322d;
            float rr=r*(.18f+(i%47)/52f);
            float sx=x+(float)Math.cos(a+spin*.003f)*rr;
            float sy=y+(float)Math.sin(a+spin*.003f)*rr;
            float brightness=.15f+(i%9)*.085f;
            fillCircle(c,p,sx,sy,r*(i%7==0?.009f:.0042f),
                alpha(i%3==0?accent:Color.WHITE,brightness));
        }
        p.setShader(new RadialGradient(x,y,r*.95f,new int[]{
            alpha(0xffc469ff,.19f),alpha(0xff5536dd,.23f),
            alpha(0xffb466ff,.07f),Color.TRANSPARENT
        },null,Shader.TileMode.CLAMP));
        c.drawCircle(x,y,r*.95f,p);p.setShader(null);
        metal(c,p,x,y,r,mix(accent,0xff344bd8,.60f),false);
        fillCircle(c,p,x,y,r*.845f,0xff120c2c);
        for(int i=0;i<6;i++){
            float rr=r*(.78f-i*.055f);
            arcGlow(c,p,x,y,rr,r*(i%2==0?.014f:.006f),
                spin*(i%2==0?.42f:-.28f)+i*42f,220f-i*14f,
                i%2==0?accent:0xfffa4cd2,glow);
        }
        for(int i=0;i<34;i++){
            float a=(float)(i*2*Math.PI/34.0+Math.toRadians(spin*.18f));
            float rr=r*(.51f+(i%6)*.050f);
            dot(c,p,x+(float)Math.cos(a)*rr,y+(float)Math.sin(a)*rr,
                r*(i%4==0?.010f:.005f),i%2==0?accent:0xffe155ff,glow);
        }
        drawDigits(c,p,x,y,r,accent,spin*.13f,false);
        centerDisc(c,p,x,y,r,accent,true);
        stroke(c,p,x,y,r*.34f,r*.013f,alpha(accent,.76f));
        arcGlow(c,p,x,y,r*.35f,r*.017f,spin*.7f,112f,accent,glow);
    }
    private static void arcReactor(Canvas c,Paint p,float x,float y,float r,
                                   int accent,float spin,float glow){
        metal(c,p,x,y,r,accent,false);
        fillCircle(c,p,x,y,r*.846f,0xff061b2b);
        stroke(c,p,x,y,r*.84f,r*.012f,0xff477b9a);
        // Twelve self-contained blue light coils; each unit has a metallic casing.
        for(int i=0;i<12;i++){
            c.save();c.rotate(i*30f+spin*.10f,x,y);
            RectF housing=new RectF(x-r*.079f,y-r*.83f,
                x+r*.079f,y-r*.61f);
            p.setShader(new LinearGradient(x-r*.08f,y-r*.83f,x+r*.08f,y-r*.61f,
                new int[]{0xff4c657b,0xff101b29,0xff87a4b9},null,
                Shader.TileMode.CLAMP));
            p.setStyle(Paint.Style.FILL);c.drawRoundRect(housing,r*.021f,r*.021f,p);
            p.setShader(null);
            p.setColor(alpha(accent,.25f*glow));
            p.setShadowLayer(r*.045f,0,0,accent);
            c.drawRoundRect(new RectF(x-r*.055f,y-r*.797f,x+r*.055f,y-r*.65f),
                r*.012f,r*.012f,p);
            p.clearShadowLayer();
            p.setColor(mix(accent,Color.WHITE,.48f));
            c.drawRoundRect(new RectF(x-r*.044f,y-r*.779f,x+r*.044f,y-r*.664f),
                r*.010f,r*.010f,p);
            c.restore();
        }
        glowing(c,p,x,y,r*.60f,r*.028f,accent,glow);
        stroke(c,p,x,y,r*.546f,r*.034f,0xff829db4);
        stroke(c,p,x,y,r*.508f,r*.017f,alpha(accent,.75f));
        for(int i=0;i<8;i++){
            arcGlow(c,p,x,y,r*.485f,r*.012f,i*45f-spin*.48f,23f,accent,glow);
        }
        // Original triangular energy core, with three structural supports.
        Path outer=new Path();
        for(int i=0;i<3;i++){
            float a=(float)Math.toRadians(-90+i*120);
            float px=x+(float)Math.cos(a)*r*.435f;
            float py=y+(float)Math.sin(a)*r*.435f;
            if(i==0)outer.moveTo(px,py);else outer.lineTo(px,py);
        }
        outer.close();
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(x,y,r*.52f,
            new int[]{alpha(accent,.86f),0xff133f57,0xff091724},
            null,Shader.TileMode.CLAMP));
        c.drawPath(outer,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeWidth(r*.031f);p.setColor(0xffb5c9d9);
        c.drawPath(outer,p);
        p.setStrokeWidth(r*.013f);p.setColor(accent);
        p.setShadowLayer(r*.05f,0,0,accent);c.drawPath(outer,p);
        p.clearShadowLayer();p.setStyle(Paint.Style.FILL);
        Path inner=new Path();
        for(int i=0;i<3;i++){
            float a=(float)Math.toRadians(-90+i*120);
            float px=x+(float)Math.cos(a)*r*.30f;
            float py=y+(float)Math.sin(a)*r*.30f;
            if(i==0)inner.moveTo(px,py);else inner.lineTo(px,py);
        }
        inner.close();p.setColor(0xff071b2c);c.drawPath(inner,p);
        stroke(c,p,x,y,r*.21f,r*.018f,alpha(accent,.8f));
        dot(c,p,x,y,r*.055f,0xffdafbff,glow);
    }
    private static void drawClock(Canvas c,Paint p,float x,float y,float r,
                                  int id,int accent,float glow,int centerStyle){
        if(centerStyle==1){
            stroke(c,p,x,y,r*.39f,r*.008f,
                alpha(accent,.62f*glow));
        }else if(centerStyle==2){
            arcGlow(c,p,x,y,r*.37f,r*.008f,190f,145f,accent,.6f*glow);
            p.setStrokeWidth(r*.004f);p.setStyle(Paint.Style.STROKE);
            p.setColor(alpha(accent,.36f));
            c.drawLine(x-r*.25f,y+r*.235f,x+r*.25f,y+r*.235f,p);
            p.setStyle(Paint.Style.FILL);
        }else if(centerStyle==3){
            // Minimal face: no extra circular HUD graphics.
        }
        Calendar now=Calendar.getInstance();
        String hhmm=String.format(Locale.getDefault(),"%02d:%02d",
            now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE));
        float size=r*.226f;
        p.setShader(null);p.setStyle(Paint.Style.FILL);
        p.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
        p.setTextSize(size);p.setTextAlign(Paint.Align.CENTER);
        float measured=p.measureText(hhmm);
        if(measured>r*.70f)p.setTextSize(size*(r*.70f/measured));
        float baseline=y-(p.ascent()+p.descent())*.5f+r*.12f;
        p.setColor(id==ThemePrefs.GOLD?0xfffff2d9:0xfff4fbff);
        p.setShadowLayer(r*.025f,0,0,alpha(accent,.95f*glow));
        c.drawText(hhmm,x,baseline,p);
        p.clearShadowLayer();
    }
    private static void drawEntryPulse(Canvas c,int w,int h,Context context,
                                       int id,float fraction){
        int accent=ThemePrefs.effectiveAccent(context,id);
        float x=w*.5f;
        float y=h*(ThemePrefs.verticalForScreen(context,id,false)/100f);
        float r=Math.min(w*.47f,h*.26f);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        arcGlow(c,p,x,y,r*.97f,r*.018f,-90f+fraction*360f,
            125f,accent,1f-fraction);
    }
    private static void lettering(Canvas c,Paint p,String text,float x,float y,
                                  float size,int color,boolean bold){
        p.setShader(null);p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));
        p.setTextSize(size);p.setColor(color);
        p.clearShadowLayer();
        c.drawText(text,x,y,p);
    }
}
