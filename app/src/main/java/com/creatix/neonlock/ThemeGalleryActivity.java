package com.creatix.neonlock;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.graphics.Canvas;
import java.util.ArrayList;

/** Launch screen: seven live-previews and a direct one-touch theme picker. */
public final class ThemeGalleryActivity extends Activity {
    private LinearLayout gallery;
    private TextView activeTitle;
    private final ArrayList<ThemePreview> previews=new ArrayList<>();

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setStatusBarColor(0xff040713);
        getWindow().setNavigationBarColor(0xff040713);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xff040713);
        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16),dp(26),dp(16),dp(28));
        scroll.addView(content);

        TextView brand=label("CR3@TIX  /  ANDROID",12,0xff3afbbf,true);
        brand.setLetterSpacing(.18f);
        add(content,brand,0);
        TextView title=label("NEON LOCK",32,0xffedf6ff,true);
        title.setLetterSpacing(.07f);
        add(content,title,9);
        TextView edition=label("7 THÈMES  •  FONDS ANIMÉS 3D",14,0xff92b8d5,true);
        add(content,edition,2);
        activeTitle=label("",13,0xffc3d7e8,false);
        add(content,activeTitle,13);

        gallery=new LinearLayout(this);
        gallery.setOrientation(LinearLayout.VERTICAL);
        add(content,gallery,12);
        renderGallery();

        Button apply=MainActivity.button(this,"◉  ACTIVER LE FOND D'ÉCRAN ANIMÉ",
            0xff41e7c7,Color.BLACK);
        apply.setOnClickListener(v->activateWallpaper());
        add(content,apply,14);

        Button personalize=MainActivity.button(this,"⚙  PERSONNALISER LE THÈME",
            0xff18263a,0xfff5fbff);
        personalize.setOnClickListener(v->startActivity(
            new Intent(this,ThemeSettingsActivity.class)));
        add(content,personalize,9);

        Button updates=MainActivity.button(this,"↻  VÉRIFIER LES MISES À JOUR",
            0xff18263a,0xfff5fbff);
        updates.setOnClickListener(v->NeonUpdates.check(this,true));
        add(content,updates,9);

        Button oldOptions=MainActivity.button(this,"ⓘ  INFORMATIONS ET FOND ORIGINAL",
            0xff121d2c,0xffb8cbde);
        oldOptions.setOnClickListener(v->startActivity(new Intent(this,MainActivity.class)));
        add(content,oldOptions,9);

        TextView desc=label(
            "Choisis un thème : il s'applique immédiatement au fond d'écran s'il est actif. "+
            "Sinon, utilise « Activer le fond d'écran animé ». "+
            "Chaque thème mémorise ses propres réglages.",12,0xff899fb8,false);
        desc.setGravity(Gravity.CENTER);
        add(content,desc,14);

        TextView notice=label(
            "ARC REACTOR est une création originale de science-fiction, "+
            "sans affiliation avec Marvel ou Iron Man.",11,0xff647c99,false);
        notice.setGravity(Gravity.CENTER);
        add(content,notice,8);
        setContentView(scroll);
        NeonUpdates.schedule(this);
    }

    private int dp(int n){return MainActivity.dp(this,n);}
    private TextView label(String text,int size,int color,boolean bold){
        TextView t=MainActivity.text(this,text,size,color,bold);
        t.setGravity(Gravity.CENTER);
        return t;
    }
    private void add(LinearLayout parent,View child,int margin){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(margin);
        parent.addView(child,lp);
    }
    private GradientDrawable panel(int theme,boolean selected){
        int base=0xff101b29;
        int edge=ThemePrefs.accent(this,theme);
        GradientDrawable drawable=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{0xff1b2c3c,base,0xff080f1a});
        drawable.setCornerRadius(dp(15));
        drawable.setStroke(dp(selected?2:1),selected?edge:0xff38485a);
        return drawable;
    }
    private void renderGallery(){
        gallery.removeAllViews();
        previews.clear();
        int selected=ThemePrefs.selected(this),active=ThemePrefs.active(this);
        activeTitle.setText("THÈME ACTIF  •  "+ThemePrefs.name(active)+
            (ThemePrefs.autoChange(this)?"   ↻ AUTO":""));
        for(int row=0;row<4;row++){
            LinearLayout pair=new LinearLayout(this);
            pair.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp=new LinearLayout.LayoutParams(-1,-2);
            rowLp.topMargin=dp(8);
            gallery.addView(pair,rowLp);
            for(int col=0;col<2;col++){
                final int index=row*2+col;
                if(index>=ThemePrefs.COUNT){
                    View spacer=new View(this);
                    pair.addView(spacer,new LinearLayout.LayoutParams(0,dp(1),1f));
                    continue;
                }
                final boolean isSelected=index==active;
                LinearLayout card=new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackground(panel(index,isSelected));
                card.setPadding(dp(5),dp(5),dp(5),dp(11));
                LinearLayout.LayoutParams cardLp=new LinearLayout.LayoutParams(0,-2,1f);
                cardLp.setMargins(dp(col==0?0:4),0,dp(col==0?4:0),0);
                pair.addView(card,cardLp);

                ThemePreview preview=new ThemePreview(this,index);
                previews.add(preview);
                card.addView(preview,new LinearLayout.LayoutParams(-1,dp(156)));

                TextView name=label((index+1)+". "+ThemePrefs.name(index),
                    12,0xfff0f5ff,true);
                name.setMinHeight(dp(25));
                add(card,name,3);
                TextView subtitle=label(isSelected?"● THÈME ACTIF":
                    ThemePrefs.SUBTITLES[index],11,
                    isSelected?ThemePrefs.accent(this,index):0xff9aacc2,false);
                add(card,subtitle,1);

                card.setClickable(true);card.setFocusable(true);
                card.setContentDescription("Choisir "+ThemePrefs.name(index));
                card.setOnClickListener(v->{
                    ThemePrefs.select(this,index);
                    renderGallery();
                    Toast.makeText(this,
                        ThemePrefs.name(index)+" sélectionné",Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
    private void activateWallpaper(){
        try{
            Intent intent=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this,MainActivity.LiveWallpaper.class));
            startActivity(intent);
        }catch(Exception e){
            try{startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));}
            catch(Exception ignored){
                Toast.makeText(this,"Sélecteur Android indisponible.",Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override protected void onResume(){
        super.onResume();
        if(gallery!=null)renderGallery();
        NeonUpdates.checkDownloaded(this,false);
        NeonUpdates.check(this,false);
    }

    /** Preview refreshes at 10 fps to minimize CPU load while scrolling cards. */
    static final class ThemePreview extends View{
        private final int theme;
        private boolean attached;
        private final Handler handler=new Handler(Looper.getMainLooper());
        private final Runnable tick=new Runnable(){
            @Override public void run(){
                if(!attached)return;
                invalidate();
                handler.postDelayed(this,100);
            }
        };
        ThemePreview(Context context,int index){super(context);theme=index;}
        @Override protected void onAttachedToWindow(){
            super.onAttachedToWindow();
            attached=true;handler.removeCallbacks(tick);handler.post(tick);
        }
        @Override protected void onDetachedFromWindow(){
            attached=false;handler.removeCallbacks(tick);super.onDetachedFromWindow();
        }
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            ThemeArt.draw(canvas,getWidth(),getHeight(),getContext(),true,theme);
        }
    }
}
