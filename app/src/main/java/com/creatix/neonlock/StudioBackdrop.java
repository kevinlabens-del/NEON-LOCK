package com.creatix.neonlock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/** Locally imported user scenery; no storage permission and no internet needed. */
final class StudioBackdrop {
    private static final LinkedHashMap<Integer,Bitmap> CACHE=new LinkedHashMap<Integer,Bitmap>(
            4,.75f,true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Integer,Bitmap> e){
            boolean remove=size()>3;
            if(remove && e.getValue()!=null && !e.getValue().isRecycled())
                e.getValue().recycle();
            return remove;
        }
    };
    private StudioBackdrop(){}

    static File file(Context c,int theme){
        return new File(c.getFilesDir(),"studio_backdrop_"+ThemePrefs.safe(theme)+".jpg");
    }
    static boolean exists(Context c,int theme){
        return file(c,theme).isFile();
    }
    static synchronized void clear(Context c,int theme){
        Bitmap cached=CACHE.remove(theme);
        if(cached!=null&&!cached.isRecycled())cached.recycle();
        file(c,theme).delete();
    }
    /** Decode a document at a bounded resolution to avoid loading huge camera photos. */
    static synchronized void importDocument(Context c,int theme,Uri uri) throws IOException {
        if(theme==ThemePrefs.VAULT)
            throw new IOException("Vault Original conserve son image d'origine.");
        BitmapFactory.Options bounds=new BitmapFactory.Options();
        bounds.inJustDecodeBounds=true;
        try(InputStream in=c.getContentResolver().openInputStream(uri)){
            if(in==null)throw new IOException("Image inaccessible");
            BitmapFactory.decodeStream(in,null,bounds);
        }
        int width=bounds.outWidth,height=bounds.outHeight;
        if(width<=0||height<=0)throw new IOException("Format d'image non reconnu");
        if((long)width*height>180000000L)
            throw new IOException("Image trop grande");
        int sample=1;
        while(width/sample>1600||height/sample>2200)sample*=2;
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize=sample;
        options.inPreferredConfig=Bitmap.Config.RGB_565;
        Bitmap bitmap;
        try(InputStream in=c.getContentResolver().openInputStream(uri)){
            if(in==null)throw new IOException("Image inaccessible");
            bitmap=BitmapFactory.decodeStream(in,null,options);
        }catch(OutOfMemoryError error){
            throw new IOException("Pas assez de mémoire pour cette image");
        }
        if(bitmap==null)throw new IOException("Décodage de l'image impossible");
        File target=file(c,theme);
        File staging=new File(c.getCacheDir(),"studio-import-temp.jpg");
        try(FileOutputStream out=new FileOutputStream(staging,false)){
            if(!bitmap.compress(Bitmap.CompressFormat.JPEG,86,out))
                throw new IOException("Impossible d'enregistrer la photo");
            out.getFD().sync();
        }finally{
            bitmap.recycle();
        }
        Bitmap old=CACHE.remove(theme);
        if(old!=null&&!old.isRecycled())old.recycle();
        if(target.exists()&&!target.delete())throw new IOException("Remplacement impossible");
        if(!staging.renameTo(target))throw new IOException("Sauvegarde impossible");
    }
    private static synchronized Bitmap load(Context c,int theme){
        Bitmap found=CACHE.get(theme);
        if(found!=null&&!found.isRecycled())return found;
        File path=file(c,theme);
        if(!path.isFile())return null;
        try{
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inPreferredConfig=Bitmap.Config.RGB_565;
            Bitmap bitmap=BitmapFactory.decodeFile(path.getAbsolutePath(),options);
            if(bitmap!=null)CACHE.put(theme,bitmap);
            return bitmap;
        }catch(OutOfMemoryError error){return null;}
    }
    /**
     * Imported scenery is blended outside the central mechanism so the animated
     * numbers and HH:mm stay readable; the original Vault never gets an overlay.
     */
    static void draw(Canvas canvas,Context context,int width,int height,
                     int theme,float cx,float cy,float radius){
        if(theme==ThemePrefs.VAULT || !exists(context,theme))return;
        Bitmap image=load(context,theme);
        if(image==null)return;
        canvas.save();
        Path cutout=new Path();
        cutout.setFillType(Path.FillType.EVEN_ODD);
        cutout.addRect(0,0,width,height,Path.Direction.CW);
        cutout.addCircle(cx,cy,radius*.91f,Path.Direction.CW);
        canvas.clipPath(cutout);
        Paint paint=new Paint(Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
        paint.setAlpha(142);
        float imageRatio=(float)image.getWidth()/Math.max(1,image.getHeight());
        float viewRatio=(float)width/Math.max(1,height);
        Rect src;
        if(imageRatio>viewRatio){
            int crop=Math.max(1,Math.round(image.getHeight()*viewRatio));
            int left=Math.max(0,(image.getWidth()-crop)/2);
            src=new Rect(left,0,Math.min(image.getWidth(),left+crop),image.getHeight());
        }else{
            int crop=Math.max(1,Math.round(image.getWidth()/viewRatio));
            int top=Math.max(0,(image.getHeight()-crop)/2);
            src=new Rect(0,top,image.getWidth(),Math.min(image.getHeight(),top+crop));
        }
        canvas.drawBitmap(image,src,new RectF(0,0,width,height),paint);
        canvas.restore();
    }
}
