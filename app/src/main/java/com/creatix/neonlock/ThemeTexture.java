package com.creatix.neonlock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

/**
 * One compressed HD artwork atlas for the six newly added 3D styles.
 * The atlas is decoded once and cropped into six rings, not decoded per frame.
 * This keeps the actual photograph-like metal and nebula materials visible,
 * while animated readouts, digits and effects are rendered separately.
 */
final class ThemeTexture {
    private static Bitmap atlas;
    private static boolean attempted;
    private ThemeTexture(){}
    private static synchronized Bitmap getAtlas(Context context){
        if(!attempted){
            attempted=true;
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inScaled=false;
            try{atlas=BitmapFactory.decodeResource(context.getResources(),
                R.drawable.neon_v140_hd_atlas,options);}
            catch(OutOfMemoryError error){atlas=null;}
        }
        return atlas;
    }
    static boolean draw(Canvas c,Paint p,Context context,float x,float y,float r,
                        int theme,int accent,float spin,float glow,
                        float xOffset,float seconds){
        Bitmap image=getAtlas(context);
        if(image==null || theme<1 || theme>6)return false;
        int index=theme-1,col=index%3,row=index/3;
        int tw=image.getWidth()/3,th=image.getHeight()/2;
        // Exclude thin dividers between the six original illustrations.
        int insetX=Math.max(2,tw/190),insetY=Math.max(2,th/105);
        Rect src=new Rect(col*tw+insetX,row*th+insetY,
            (col+1)*tw-insetX,(row+1)*th-insetY);
        float dx=ThemeMath.parallax(xOffset,r*.07f);
        RectF dst=new RectF(x-r+dx,y-r*.99f,x+r+dx,y+r*.99f);
        Path mask=new Path();
        mask.addCircle(x,y,r,Path.Direction.CW);
        c.save();
        c.clipPath(mask);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);p.setAlpha(255);
        p.setFilterBitmap(true);p.clearShadowLayer();p.setShader(null);
        c.drawBitmap(image,src,dst,p);
        c.restore();

        // Lighting overlays inherit each theme's user-selected accent.
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(r*.015f);
        p.setColor(withAlpha(accent,.49f*glow));
        c.drawCircle(x,y,r*.48f,p);
        p.setStrokeWidth(r*.008f);
        p.setColor(withAlpha(Color.WHITE,.34f));
        c.drawCircle(x,y,r*.91f,p);
        p.setStyle(Paint.Style.FILL);
        drawMaterial(c,p,x,y,r,theme,accent,glow,
            ThemePrefs.material(context,theme));
        drawMotion(c,p,x,y,r,theme,accent,glow,spin,seconds);
        digits(c,p,x,y,r,accent,spin,ThemePrefs.digitStyle(context,theme));
        return true;
    }

    private static int withAlpha(int color,float alpha){
        return Color.argb((int)(ThemeMath.clamp(alpha,0f,1f)*255f),
            Color.red(color),Color.green(color),Color.blue(color));
    }
    private static void arc(Canvas c,Paint p,float x,float y,float r,
                            float angle,float sweep,float width,int color){
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(width);
        p.setColor(color);p.setShader(null);
        c.drawArc(new RectF(x-r,y-r,x+r,y+r),angle,sweep,false,p);
        p.setStyle(Paint.Style.FILL);
    }
    private static void glowCircle(Canvas c,Paint p,float x,float y,float rad,
                                   int accent,float opacity){
        p.setShader(new RadialGradient(x,y,rad,
            new int[]{withAlpha(accent,opacity),withAlpha(accent,opacity*.22f),
                Color.TRANSPARENT},null,Shader.TileMode.CLAMP));
        p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,rad,p);p.setShader(null);
    }
    private static void drawMaterial(Canvas c,Paint p,float x,float y,float r,
                                     int theme,int color,float glow,int material){
        if(material==0)return;
        if(material==1){ // titanium brushed highlights
            for(int i=0;i<20;i++)
                arc(c,p,x,y,r*(.85f+i*.0044f),200+i*5f,
                    47f,r*.0022f,withAlpha(Color.WHITE,.18f));
        }else if(material==2){ // brass/rose-gold trim
            for(int i=0;i<4;i++){
                int warm=i%2==0?0xffffd084:0xffb27236;
                arc(c,p,x,y,r*(.90f-i*.035f),-30+i*9f,154f,
                    r*.0048f,withAlpha(warm,.46f*glow));
            }
        }else if(material==3){ // carbon fiber cross-weave
            p.setStyle(Paint.Style.STROKE);
            p.setColor(withAlpha(Color.BLACK,.28f));
            p.setStrokeWidth(r*.007f);
            c.save();
            Path mask=new Path();
            mask.addCircle(x,y,r*.99f,Path.Direction.CW);
            mask.addCircle(x,y,r*.55f,Path.Direction.CCW);
            c.clipPath(mask);
            for(int i=-14;i<=14;i++){
                float yy=y+i*r*.080f;
                c.drawLine(x-r,yy-r*.2f,x+r,yy+r*.2f,p);
            }
            c.restore();p.setStyle(Paint.Style.FILL);
        }
    }
    private static void drawMotion(Canvas c,Paint p,float x,float y,float r,
                                   int theme,int accent,float glow,float spin,float sec){
        float speed=sec*.5f;
        switch(theme){
            case ThemePrefs.CYBER:
                // Cyan scanner bars glide across HUD panes while rim stays fixed.
                arc(c,p,x,y,r*.76f,spin,70f,r*.013f,
                    withAlpha(accent,.85f*glow));
                arc(c,p,x,y,r*.61f,-spin*.72f,110f,r*.007f,
                    withAlpha(Color.WHITE,.55f*glow));
                float scanner=(float)Math.sin(sec*1.6f)*r*.48f;
                p.setColor(withAlpha(accent,.30f*glow));
                p.setStrokeWidth(r*.010f);p.setStyle(Paint.Style.STROKE);
                c.drawLine(x-r*.38f,y+scanner,x+r*.38f,y+scanner,p);
                p.setStyle(Paint.Style.FILL);
                break;
            case ThemePrefs.GOLD:
                // Moving jewel highlight and micro-gear pins on a gold dial.
                arc(c,p,x,y,r*.865f,spin*.25f,38f,r*.012f,
                    withAlpha(0xffffe9ae,.80f*glow));
                for(int i=0;i<12;i++){
                    double a=i*Math.PI/6+spin*.003d;
                    glowCircle(c,p,x+(float)Math.cos(a)*r*.80f,
                        y+(float)Math.sin(a)*r*.80f,
                        r*.020f,0xffffc872,.20f*glow);
                }
                break;
            case ThemePrefs.RED:
                for(int i=0;i<7;i++)
                    arc(c,p,x,y,r*.82f,-spin*.66f+i*360f/7f,
                        14f,r*.027f,withAlpha(accent,
                            (.33f+.40f*ThemeMath.pulse(sec+i*.25f,1.9f))*glow));
                arc(c,p,x,y,r*.55f,spin*1.8f,72f,r*.009f,
                    withAlpha(accent,.88f*glow));
                break;
            case ThemePrefs.STEAMPUNK:
                // Wisps of steam rise and fade from twin release valves.
                for(int i=0;i<5;i++){
                    float t=(sec*.18f+i*.2f)%1f;
                    float yy=y+r*.44f-t*r*.90f;
                    float xx=x+(i%2==0?-1:1)*r*.73f+
                        (float)Math.sin(t*5f+i)*r*.045f;
                    glowCircle(c,p,xx,yy,r*(.08f+t*.06f),
                        0xffd6aa83,.13f*(1-t)*glow);
                }
                arc(c,p,x,y,r*.65f,-spin*.24f,61f,r*.008f,
                    withAlpha(0xffd7a96c,.60f*glow));
                break;
            case ThemePrefs.GALAXY:
                for(int i=0;i<22;i++){
                    float a=(float)(i*2.39996d+sec*.05d);
                    float rad=r*(.50f+(i%6)*.065f);
                    float xx=x+(float)Math.cos(a)*rad,yy=y+(float)Math.sin(a)*rad;
                    float opacity=.12f+.48f*ThemeMath.pulse(sec+i*.26f,3.2f);
                    glowCircle(c,p,xx,yy,r*.017f,
                        i%2==0?accent:0xffef70ff,opacity*glow);
                }
                arc(c,p,x,y,r*.77f,spin*.16f,78f,r*.012f,
                    withAlpha(0xfffd80ff,.75f*glow));
                break;
            case ThemePrefs.ARC:
                // Electric coil segments pulse around a stationary reactor core.
                for(int i=0;i<12;i++){
                    float power=.25f+.6f*ThemeMath.pulse(sec+i*.11f,2.5f);
                    arc(c,p,x,y,r*.72f,i*30f+spin*.11f,
                        15f,r*.026f,withAlpha(accent,power*glow));
                }
                arc(c,p,x,y,r*.51f,-spin*.70f,90f,r*.018f,
                    withAlpha(0xffccf7ff,.82f*glow));
                break;
        }
        // A narrow moving metal reflection all themes share.
        arc(c,p,x,y,r*.93f,-90f+spin*.22f,19f,r*.006f,
            withAlpha(Color.WHITE,.44f*glow));
    }

    private static void digits(Canvas c,Paint p,float x,float y,float r,
                               int accent,float spin,int style){
        p.setShader(null);p.setStyle(Paint.Style.FILL);
        p.setTypeface(Typeface.create(style==1?"serif":
            (style==2?"monospace":"sans-serif-condensed"),Typeface.BOLD));
        p.setTextSize(r*(style==1?.135f:.152f));
        p.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm=p.getFontMetrics();
        for(int i=0;i<7;i++){
            float angle=(float)Math.toRadians(ThemeMath.orbitAngle(i,
                spin/6f,1f,false));
            float xx=x+(float)Math.cos(angle)*r*.595f;
            float yy=y+(float)Math.sin(angle)*r*.595f-
                (fm.ascent+fm.descent)*.5f;
            String numeral=Integer.toString(i);
            p.setColor(withAlpha(Color.BLACK,.75f));
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.020f);
            c.drawText(numeral,xx,yy,p);
            p.setStyle(Paint.Style.FILL);
            p.setShadowLayer(r*.017f,0f,0f,accent);
            p.setColor(style==2?Color.WHITE:withAlpha(accent,1f));
            c.drawText(numeral,xx,yy,p);
            p.clearShadowLayer();
        }
    }
}
