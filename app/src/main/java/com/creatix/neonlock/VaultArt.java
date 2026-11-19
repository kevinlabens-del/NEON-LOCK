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
    private static Bitmap wheelSource;
    private static Bitmap wheelTinted;
    private static int wheelTintedAccent=0;

    /**
     * Digit-free version of the exact source photo. Only this inner wheel
     * rotates. Numerals are drawn upright by Canvas after rotation is restored.
     */
    private static synchronized Bitmap getWheelArt(Context context,int accent){
        if(wheelSource==null){
            BitmapFactory.Options opts=new BitmapFactory.Options();
            opts.inScaled=false;
            wheelSource=BitmapFactory.decodeResource(context.getResources(),
                R.drawable.neon_vault_wheel_clean,opts);
        }
        if(wheelSource==null)return null;
        if(accent==MainActivity.ORANGE)return wheelSource;
        if(wheelTinted!=null && wheelTintedAccent==accent)return wheelTinted;

        int w=wheelSource.getWidth(),h=wheelSource.getHeight();
        int[] pixels=new int[w*h];
        wheelSource.getPixels(pixels,0,w,0,0,w,h);
        int nr=Color.red(accent),ng=Color.green(accent),nb=Color.blue(accent);
        for(int i=0;i<pixels.length;i++){
            int rgb=pixels[i];
            int a=Color.alpha(rgb),r=Color.red(rgb);
            int g=Color.green(rgb),b=Color.blue(rgb);
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
                pixels[i]=Color.argb(a,r,g,b);
            }
        }
        Bitmap result=Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888);
        if(wheelTinted!=null && !wheelTinted.isRecycled())wheelTinted.recycle();
        wheelTinted=result;
        wheelTintedAccent=accent;
        return result;
    }



    private static synchronized Bitmap getArt(Context context,int accent){
        if(source==null){
            BitmapFactory.Options opts=new BitmapFactory.Options();
            opts.inScaled=false;
            source=BitmapFactory.decodeResource(context.getResources(),
                R.drawable.neon_vault_center_clean,opts);
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
        draw(canvas,width,height,context,preview,
            ThemePrefs.vertical(context,ThemePrefs.VAULT)/100f,.5f);
    }
    static void draw(Canvas canvas,int width,int height,Context context,boolean preview,
                     float verticalFraction,float xOffset){
        if(width<=0||height<=0)return;
        LockPrefs.Style style=LockPrefs.get(context);
        int selectedAccent=ThemePrefs.prefs(context).contains("t0_accent")?
            ThemePrefs.accent(context,ThemePrefs.VAULT):style.ringColor;
        if(ThemePrefs.nightPalette(context,ThemePrefs.VAULT) &&
           ThemeMath.isNight(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))){
            int night=ThemePrefs.nightColor(context,ThemePrefs.VAULT);
            selectedAccent=Color.rgb(
                (Color.red(selectedAccent)+Color.red(night))/2,
                (Color.green(selectedAccent)+Color.green(night))/2,
                (Color.blue(selectedAccent)+Color.blue(night))/2);
        }
        Bitmap img=getArt(context,selectedAccent);
        Bitmap wheelImage=getWheelArt(context,selectedAccent);
        if(img==null || wheelImage==null){
            canvas.drawColor(MainActivity.BG);
            return;
        }
        final float w=width,h=height;
        final float cx=w*.5f;
        final float cy=h*(preview?.51f:ThemeMath.clamp(verticalFraction,.35f,.80f));
        float r=Math.min(w*.475f,h*(preview?.43f:.27f))*
            ThemePrefs.size(context,ThemePrefs.VAULT)/100f;
        r=Math.min(r,Math.min(cy*.97f,(h-cy)*.96f));
        if(r<10f)return;
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        p.setDither(true);
        final int accent=selectedAccent;
        int currentGlow=ThemePrefs.prefs(context).contains("t0_glow")?
            ThemePrefs.glow(context,ThemePrefs.VAULT):style.neonIntensity;
        if(ThemePrefs.adaptive(context,ThemePrefs.VAULT) &&
           ThemeMath.isNight(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)))
            currentGlow=Math.round(currentGlow*.67f);
        final int glowAlpha=Math.max(40,Math.min(245,currentGlow));

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
        float parallax=ThemePrefs.parallaxEnabled(context)?
            ThemeMath.parallax(xOffset,r*.012f):0f;
        RectF dst=new RectF(cx-r+parallax,cy-r,cx+r+parallax,cy+r);

        // Static external steel rim and orange triangular pointer.
        canvas.save();
        Path disc=new Path();
        disc.addCircle(cx,cy,r,Path.Direction.CW);
        canvas.clipPath(disc);
        drawOriginal(canvas,p,img,src,dst);
        canvas.restore();

        // A separate digit-free metal photo rotates underneath its upright numerals.
        // The reference artwork (including the triangular pointer) stays stationary.
        Calendar now=Calendar.getInstance();
        float elapsed=now.get(Calendar.SECOND)
            +now.get(Calendar.MILLISECOND)/1000f;
        boolean reversed=ThemePrefs.prefs(context).contains("t0_reverse")?
            ThemePrefs.reverse(context,ThemePrefs.VAULT):style.reverseRotation;
        float wheelAngle=360f/7f+(reversed?-1f:1f)*elapsed*6f*
            ThemePrefs.speed(context,ThemePrefs.VAULT)/100f;
        canvas.save();
        Path annulus=new Path();
        annulus.setFillType(Path.FillType.EVEN_ODD);
        annulus.addCircle(cx,cy,r*.800f,Path.Direction.CW);
        annulus.addCircle(cx,cy,r*.488f,Path.Direction.CW);
        canvas.clipPath(annulus);
        canvas.rotate(wheelAngle,cx,cy);
        drawOriginal(canvas,p,wheelImage,src,dst);
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

        // Draw seven numbers individually after restoring the canvas.
        // Only their POSITIONS rotate; the glyphs always remain vertical.
        // This also prevents upside-down digits seen in the user's video.
        p.setShader(null);
        p.setTextAlign(Paint.Align.CENTER);
        int digitStyle=ThemePrefs.digitStyle(context,ThemePrefs.VAULT);
        p.setTypeface(Typeface.create(digitStyle==1?"serif":
            digitStyle==2?"monospace":"sans-serif-condensed",Typeface.BOLD));
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setTextSize(r*.145f);
        Paint.FontMetrics fm=p.getFontMetrics();
        for(int digit=0;digit<=6;digit++){
            float angleDeg=digit*(360f/7f)-90f+wheelAngle;
            float angle=(float)Math.toRadians(angleDeg);
            float nx=cx+(float)Math.cos(angle)*r*.615f;
            float ny=cy+(float)Math.sin(angle)*r*.615f;
            float baseline=ny-(fm.ascent+fm.descent)*.5f;
            // An orange outline and glow retain the original neon style.
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(r*.014f);
            p.setColor(accent);
            p.setShadowLayer(r*.022f,0,0,accent);
            canvas.drawText(Integer.toString(digit),nx,baseline,p);
            p.clearShadowLayer();
            p.setStyle(Paint.Style.FILL);
            p.setColor(style.numberColor);
            p.setShadowLayer(r*.010f,0,0,accent);
            canvas.drawText(Integer.toString(digit),nx,baseline,p);
            p.clearShadowLayer();
        }
        p.setStyle(Paint.Style.FILL);

        // Subtle selectable material finish stays within the round metal bezel.
        int material=ThemePrefs.material(context,ThemePrefs.VAULT);
        if(material!=0){
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(r*.008f);
            p.setColor(material==2?0xffefc386:
                material==3?0xff34475b:0xffd8ecfa);
            canvas.drawCircle(cx,cy,r*.940f,p);
            p.setStyle(Paint.Style.FILL);
        }

        // The supplied graphic has been retouched to remove its baked-in time.
        // Its central glass/steel face is the REAL BACKDROP; do not draw a
        // rectangle, rounded banner, dark mask, or border behind the digits.
        // Hours, minutes and seconds are placed directly onto the central round.

        int centerStyle=ThemePrefs.centerStyle(context,ThemePrefs.VAULT);
        if(centerStyle==1 || centerStyle==2){
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(r*.008f);
            p.setColor(accent);
            if(centerStyle==1)canvas.drawCircle(cx,cy+r*.06f,r*.37f,p);
            else canvas.drawArc(new RectF(cx-r*.38f,cy-r*.32f,
                cx+r*.38f,cy+r*.44f),210f,115f,false,p);
            p.setStyle(Paint.Style.FILL);
        }
        // Central circle shows ONLY hours and minutes (HH:mm).
        // Seconds remain available as wheel motion, not as central text.
        String time=String.format(Locale.getDefault(),"%02d:%02d",
            now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE));
        float size=r*.276f;
        p.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
        p.setTextSize(size);
        float textWidth=p.measureText(time);
        if(textWidth>r*.70f){
            size*=r*.70f/textWidth;
            p.setTextSize(size);
        }
        p.setTextAlign(Paint.Align.CENTER);
        p.setStyle(Paint.Style.FILL);
        p.setShader(null);
        p.setColor(style.clockColor);
        p.setShadowLayer(size*.14f,0f,0f,Color.argb(95,210,231,255));
        canvas.drawText(time,cx,cy+r*.17f,p);
        p.clearShadowLayer();

        // Retain the optional countdown setting, but NEVER show seconds
        // inside the center circle. Put the counter BELOW the entire dial.
        if(style.showCountdown){
            int remaining=60-now.get(Calendar.SECOND);
            float outsideY=Math.min(h-r*.09f,cy+r*1.155f);
            if(outsideY>cy+r*1.025f){
                drawText(canvas,p,String.format(Locale.getDefault(),
                    "%02d SEC RESTANTES",remaining),cx,outsideY,
                    r*.079f,style.numberColor,true,false);
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
