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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * User-supplied artwork, displayed in its original 3D photographic style.
 * A ring cut from that same original artwork rotates while the outer bezel,
 * marker and central display remain fixed. No accessibility or unlock access.
 */
final class VaultArt {
    private static Bitmap source;
    private static Bitmap recolored;
    private static int recoloredAccent=0;
    private VaultArt(){}

    private static synchronized Bitmap getArt(Context context,int accent){
        if(source==null){
            BitmapFactory.Options opts=new BitmapFactory.Options();
            opts.inScaled=false;
            source=BitmapFactory.decodeResource(context.getResources(),
                R.drawable.neon_vault_reference,opts);
        }
        if(source==null)return null;
        // The supplied artwork's natural amber/orange is preserved by default.
        if(accent==MainActivity.ORANGE)return source;
        if(recolored!=null && recoloredAccent==accent)return recolored;

        final int w=source.getWidth(),h=source.getHeight();
        int[] colors=new int[w*h];
        source.getPixels(colors,0,w,0,0,w,h);
        final int nr=Color.red(accent),ng=Color.green(accent),nb=Color.blue(accent);
        for(int i=0;i<colors.length;i++){
            int rgb=colors[i];
            int a=Color.alpha(rgb),r=Color.red(rgb),g=Color.green(rgb),b=Color.blue(rgb);
            // Isolate the orange light, leaving steel/chrome and blue shadows alone.
            if(r>73 && g>24 && r>g*1.18f && g>b*1.24f && r-b>52){
                float strength=Math.min(1f,Math.max(0f,(r-g-12f)/65f));
                float light=Math.min(1f,(r+g*.33f)/280f);
                float white=Math.max(0f,Math.min(1f,(g-110f)/170f));
                int tr=Math.min(255,Math.round(nr*light+white*80f));
                int tg=Math.min(255,Math.round(ng*light+white*80f));
                int tb=Math.min(255,Math.round(nb*light+white*80f));
                r=Math.min(255,Math.max(0,Math.round(r*(1f-strength)+tr*strength)));
                g=Math.min(255,Math.max(0,Math.round(g*(1f-strength)+tg*strength)));
                b=Math.min(255,Math.max(0,Math.round(b*(1f-strength)+tb*strength)));
                colors[i]=Color.argb(a,r,g,b);
            }
        }
        Bitmap colored=Bitmap.createBitmap(colors,w,h,Bitmap.Config.ARGB_8888);
        if(recolored!=null && !recolored.isRecycled())recolored.recycle();
        recolored=colored;
        recoloredAccent=accent;
        return colored;
    }

    private static void drawText(Canvas c,Paint p,String text,float x,float baseline,
                                 float size,int color,boolean bold,boolean glow){
        p.setStyle(Paint.Style.FILL);
        p.setShader(null);
        p.setTextSize(size);
        p.setTypeface(Typeface.create(bold?"sans-serif-condensed":"sans-serif",
            bold?Typeface.BOLD:Typeface.NORMAL));
        p.setTextAlign(Paint.Align.CENTER);
        p.setColor(color);
        if(glow)p.setShadowLayer(size*.18f,0f,0f,color);
        c.drawText(text,x,baseline,p);
        p.clearShadowLayer();
    }

    private static void drawOriginal(Canvas c,Paint p,Bitmap bitmap,
                                     Rect src,RectF dst){
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);
        p.setAlpha(255);
        p.setFilterBitmap(true);
        c.drawBitmap(bitmap,src,dst,p);
    }

    static void draw(Canvas canvas,int width,int height,Context context,boolean preview){
        if(width<=0||height<=0)return;
        LockPrefs.Style style=LockPrefs.get(context);
        Bitmap img=getArt(context,style.ringColor);
        if(img==null){
            canvas.drawColor(MainActivity.BG);
            return;
        }
        final float w=width,h=height;
        final float cx=w*.5f;
        final float cy=h*(preview?.51f:style.verticalPercent/100f);
        float r=Math.min(w*.475f,h*(preview?.43f:.27f));
        r=Math.min(r,Math.min(cy*.97f,(h-cy)*.96f));
        if(r<10f)return;
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        p.setDither(true);
        final int accent=style.ringColor;
        final int glowAlpha=Math.max(40,Math.min(245,style.neonIntensity));

        // Cinematic black and midnight-blue wallpaper background.
        p.setShader(new LinearGradient(0,0,w,h,
            new int[]{Color.rgb(2,5,12),Color.rgb(5,13,28),
                Color.rgb(1,5,14),Color.rgb(3,8,19)},null,Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,w,h,p);
        p.setShader(new RadialGradient(cx,cy,r*1.93f,
            new int[]{Color.argb(106,15,46,74),Color.argb(28,20,34,57),Color.TRANSPARENT},
            null,Shader.TileMode.CLAMP));
        canvas.drawCircle(cx,cy,r*1.92f,p);
        p.setShader(null);

        // Orange atmospheric light around the outer circular bezel.
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(r*.10f);
        p.setColor(Color.argb(glowAlpha/3,Color.red(accent),
            Color.green(accent),Color.blue(accent)));
        canvas.drawCircle(cx,cy,r*.99f,p);
        p.setStyle(Paint.Style.FILL);

        // The original photo is cropped as a square centered on the safe knob.
        // Source image: 1404 x 1536, knob center approximately (702,746).
        int side=Math.min(img.getWidth(),img.getHeight());
        int sx=(img.getWidth()-side)/2;
        int sy=Math.round(img.getHeight()*.485f-side*.5f);
        sy=Math.max(0,Math.min(img.getHeight()-side,sy));
        Rect src=new Rect(sx,sy,sx+side,sy+side);
        RectF dst=new RectF(cx-r,cy-r,cx+r,cy+r);

        // Static external steel rim and orange triangular pointer.
        canvas.save();
        Path disc=new Path();
        disc.addCircle(cx,cy,r,Path.Direction.CW);
        canvas.clipPath(disc);
        drawOriginal(canvas,p,img,src,dst);
        canvas.restore();

        // Precisely cut the numbered ring from the same photo.
        // Only the inner annulus rotates; all the metal outside stays fixed.
        Calendar now=Calendar.getInstance();
        float elapsed=now.get(Calendar.SECOND)
            +now.get(Calendar.MILLISECOND)/1000f;
        float wheelAngle=360f/7f+(style.reverseRotation?-1f:1f)*elapsed*6f;
        canvas.save();
        Path annulus=new Path();
        annulus.setFillType(Path.FillType.EVEN_ODD);
        annulus.addCircle(cx,cy,r*.865f,Path.Direction.CW);
        annulus.addCircle(cx,cy,r*.485f,Path.Direction.CW);
        canvas.clipPath(annulus);
        canvas.rotate(wheelAngle,cx,cy);
        drawOriginal(canvas,p,img,src,dst);
        canvas.restore();

        // Subtle machining rings mask the seam between moving and fixed parts.
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(r*.017f);
        p.setColor(Color.rgb(116,142,163));
        canvas.drawCircle(cx,cy,r*.490f,p);
        p.setStrokeWidth(r*.020f);
        p.setColor(Color.rgb(38,56,79));
        canvas.drawCircle(cx,cy,r*.864f,p);
        p.setStrokeWidth(r*.014f);
        p.setColor(Color.argb(glowAlpha,Color.red(accent),
            Color.green(accent),Color.blue(accent)));
        canvas.drawArc(new RectF(cx-r*.915f,cy-r*.915f,
            cx+r*.915f,cy+r*.915f),25f,120f,false,p);
        p.setStyle(Paint.Style.FILL);

        // The artwork has a frozen "15:56:24" painted into it.
        // Cover only the display zone, keeping the original spindle above.
        RectF display=new RectF(cx-r*.403f,cy-r*.055f,
            cx+r*.403f,cy+r*.375f);
        p.setShader(new RadialGradient(cx,cy+r*.12f,r*.51f,
            new int[]{Color.rgb(11,23,39),Color.rgb(4,9,19),Color.rgb(1,5,12)},
            new float[]{0f,.66f,1f},Shader.TileMode.CLAMP));
        canvas.drawRoundRect(display,r*.065f,r*.065f,p);
        p.setShader(null);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(r*.007f);
        p.setColor(Color.rgb(74,93,119));
        canvas.drawRoundRect(display,r*.065f,r*.065f,p);
        p.setStyle(Paint.Style.FILL);

        // Accurate live HH:mm:ss, with the seconds on the customizable neon channel.
        String hours=String.format(Locale.getDefault(),"%02d:%02d",
            now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE));
        String seconds=String.format(Locale.getDefault(),":%02d",
            now.get(Calendar.SECOND));
        float size=r*.222f;
        p.setTextSize(size);
        p.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
        float total=p.measureText(hours)+p.measureText(seconds);
        float left=cx-total*.5f;
        p.setTextAlign(Paint.Align.LEFT);
        p.setStyle(Paint.Style.FILL);
        p.setColor(style.clockColor);
        p.setShadowLayer(size*.14f,0f,0f,Color.argb(95,210,231,255));
        float baseline=cy+r*.177f;
        canvas.drawText(hours,left,baseline,p);
        p.clearShadowLayer();
        p.setColor(accent);
        p.setShadowLayer(size*.17f,0f,0f,accent);
        canvas.drawText(seconds,left+p.measureText(hours),baseline,p);
        p.clearShadowLayer();

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(r*.008f);
        p.setColor(accent);
        canvas.drawLine(cx-r*.21f,cy+r*.245f,
            cx+r*.21f,cy+r*.245f,p);
        p.setStyle(Paint.Style.FILL);
        int remaining=60-now.get(Calendar.SECOND);
        if(style.showCountdown){
            drawText(canvas,p,String.format(Locale.getDefault(),
                "%02d SEC RESTANTES",remaining),cx,cy+r*.327f,
                r*.083f,style.numberColor,true,false);
        }else{
            for(int i=0;i<3;i++){
                p.setColor(accent);
                float barH=(i==1?.052f:.030f)*r;
                canvas.drawRoundRect(new RectF(cx+(i-1)*r*.045f-r*.010f,
                    cy+r*.300f-barH,cx+(i-1)*r*.045f+r*.010f,
                    cy+r*.300f),r*.005f,r*.005f,p);
            }
        }

        if(style.showTitle){
            drawText(canvas,p,"NEON  LOCK",cx,cy-r*1.34f,
                r*.17f,style.titleColor,true,false);
        }
        if(style.showDate){
            String date=new SimpleDateFormat("EEEE d MMMM yyyy",Locale.FRANCE)
                .format(new Date()).toUpperCase(Locale.FRANCE);
            drawText(canvas,p,date,cx,cy+r*1.30f,
                r*.106f,style.dateColor,false,false);
        }
    }
}
