package com.creatix.neonlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;

/**
 * Drag-and-pinch editor for the actual live wallpaper renderer. Nothing is
 * superimposed over Android's lock screen: touch gestures are editor-only.
 */
public final class StudioEditorActivity extends Activity {
    private static final int OPEN_PICTURE=1505;
    private int theme;
    private int editTarget=0; // 0 = scene, 1 = HH:mm
    private LinearLayout controls;
    private EditorView surface;
    private Button fullScreen;
    private Button target;
    private boolean immersive=false;
    private final Handler handler=new Handler(Looper.getMainLooper());

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        theme=ThemePrefs.selected(this);
        getWindow().setStatusBarColor(0xff02060c);
        getWindow().setNavigationBarColor(0xff02060c);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xff020713);
        LinearLayout header=new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        Button back=button("‹ RETOUR");
        back.setOnClickListener(v->finish());
        header.addView(back,new LinearLayout.LayoutParams(0,-2,1));
        fullScreen=button("APERÇU PLEIN ÉCRAN");
        fullScreen.setOnClickListener(v->setImmersive(!immersive));
        header.addView(fullScreen,new LinearLayout.LayoutParams(0,-2,2));
        root.addView(header);
        surface=new EditorView(this);
        root.addView(surface,new LinearLayout.LayoutParams(-1,0,1));
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(false);
        controls=new LinearLayout(this);
        controls.setPadding(dp(12),dp(7),dp(12),dp(20));
        controls.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(controls);
        LinearLayout.LayoutParams scrollParams=new LinearLayout.LayoutParams(-1,dp(235));
        root.addView(scroll,scrollParams);

        label("IMMERSIVE STUDIO  •  "+ThemePrefs.name(theme),16);
        label("Glisse pour déplacer • deux doigts pour redimensionner",12);
        target=button("DÉPLACER : DÉCOR");
        target.setOnClickListener(v->{
            editTarget=1-editTarget;
            target.setText(editTarget==0?"DÉPLACER : DÉCOR":"DÉPLACER : HORLOGE");
        });
        add(target);
        Button styles=button("STYLE D'HORLOGE  •  "+StudioPrefs.CLOCKS[StudioPrefs.clockStyle(this,theme)]);
        styles.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Horloge HH:mm")
            .setItems(StudioPrefs.CLOCKS,(d,index)->{
                StudioPrefs.put(this,theme,"clock_style",index);
                styles.setText("STYLE D'HORLOGE  •  "+StudioPrefs.CLOCKS[index]);
                surface.invalidate();
            }).show());
        add(styles);
        Button motion=button("ANIMATION  •  "+StudioPrefs.MOTIONS[StudioPrefs.mode(this)]);
        motion.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Intensité des animations")
            .setItems(StudioPrefs.MOTIONS,(d,index)->{
                StudioPrefs.mode(this,index);
                motion.setText("ANIMATION  •  "+StudioPrefs.MOTIONS[index]);
            }).show());
        add(motion);
        Button color=button("COULEUR HORLOGE");
        color.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Éclairage de l'heure")
            .setItems(new String[]{"Blanc","Cyan","Or","Orange","Violet","Rouge"},
                (d,i)->{
                    int[] choices={0xfff7f9ff,0xff69efff,0xffffcd77,
                        0xffffad5c,0xffd59aff,0xffff7373};
                    StudioPrefs.put(this,theme,"clock_color",choices[i]);
                    surface.invalidate();
                }).show());
        add(color);
        Button picture=button("IMPORTER UN DÉCOR DE MON TÉLÉPHONE");
        picture.setOnClickListener(v->{
            if(theme==ThemePrefs.VAULT){
                Toast.makeText(this,"Vault Original garde son décor d'origine.",
                    Toast.LENGTH_LONG).show();
                return;
            }
            Intent pick=new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("image/*");
            startActivityForResult(pick,OPEN_PICTURE);
        });
        add(picture);
        Button remove=button("RETIRER MON IMAGE PERSONNELLE");
        remove.setOnClickListener(v->{
            StudioBackdrop.clear(this,theme);
            surface.invalidate();
            Toast.makeText(this,"Décor importé retiré.",Toast.LENGTH_SHORT).show();
        });
        add(remove);
        Button advanced=button("RÉGLAGES AVANCÉS ET CRÉATIONS");
        advanced.setOnClickListener(v->startActivity(
            new Intent(this,ThemeDesignerActivity.class)));
        add(advanced);
        Button reset=button("RÉINITIALISER LA POSITION ET L'HORLOGE");
        reset.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Réinitialiser ce thème ?")
            .setMessage("Les autres thèmes et créations ne seront pas modifiés.")
            .setNegativeButton("Annuler",null)
            .setPositiveButton("Réinitialiser",(d,which)->{
                StudioPrefs.reset(this,theme);
                ThemePrefs.setInt(this,theme,"vertical",69);
                ThemePrefs.setInt(this,theme,"size",100);
                surface.invalidate();
            }).show());
        add(reset);
        Button done=button("✓ ENREGISTRER ET TERMINER");
        done.setOnClickListener(v->finish());
        add(done);
        setContentView(root);
        if(getIntent().getBooleanExtra("fullscreen",false))setImmersive(true);
    }
    private int dp(int v){return MainActivity.dp(this,v);}
    private Button button(String text){
        return MainActivity.button(this,text,0xff14283b,Color.WHITE);
    }
    private void label(String text,int size){
        TextView view=MainActivity.text(this,text,size,0xffc5dff0,size>13);
        view.setGravity(Gravity.CENTER);
        add(view);
    }
    private void add(View v){
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);
        params.topMargin=dp(6);
        controls.addView(v,params);
    }
    private void setImmersive(boolean enabled){
        immersive=enabled;
        // Hide only the settings ScrollView; the preview remains visible.
        ((View)controls.getParent()).setVisibility(
            enabled?View.GONE:View.VISIBLE);
        fullScreen.setText(enabled?"AFFICHER LES RÉGLAGES":"APERÇU PLEIN ÉCRAN");
        surface.invalidate();
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request!=OPEN_PICTURE||result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri chosen=data.getData();
        final int selected=theme;
        new Thread(()->{
            String error=null;
            try{StudioBackdrop.importDocument(getApplicationContext(),selected,chosen);}
            catch(IOException e){error=e.getMessage();}
            String failure=error;
            runOnUiThread(()->{
                if(failure!=null)new AlertDialog.Builder(this)
                    .setTitle("Import impossible").setMessage(failure)
                    .setPositiveButton("OK",null).show();
                else{
                    Toast.makeText(this,"Décor enregistré pour ce thème.",
                        Toast.LENGTH_LONG).show();
                    surface.invalidate();
                }
            });
        },"studio-picture-import").start();
    }

    private final class EditorView extends View {
        private boolean running;
        private float downY,downX,lastPinch;
        private int startVertical,startDx,startClockX,startClockY,startSize;
        private final Runnable refresh=new Runnable(){
            @Override public void run(){
                if(!running)return;
                invalidate();
                handler.postDelayed(this,StudioPrefs.mode(StudioEditorActivity.this)==
                    StudioPrefs.ECO?120:55);
            }
        };
        EditorView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onAttachedToWindow(){
            super.onAttachedToWindow();
            running=true;handler.post(refresh);
        }
        @Override protected void onDetachedFromWindow(){
            running=false;handler.removeCallbacks(refresh);
            super.onDetachedFromWindow();
        }
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            ThemeArt.draw(canvas,getWidth(),getHeight(),StudioEditorActivity.this,
                false,theme,false,.5f,1f);
            // Editor-only control handles; they are never present in the wallpaper.
            if(!immersive){
                android.graphics.Paint p=new android.graphics.Paint(3);
                float cx=getWidth()*.5f+getWidth()*.01f*
                    StudioPrefs.sceneDx(StudioEditorActivity.this,theme);
                float cy=getHeight()*ThemePrefs.vertical(StudioEditorActivity.this,theme)/100f;
                float r=Math.min(getWidth()*.45f,getHeight()*.255f)
                    *ThemePrefs.size(StudioEditorActivity.this,theme)/100f;
                if(editTarget==1){
                    cx+=r*StudioPrefs.clockDx(StudioEditorActivity.this,theme)/100f;
                    cy+=r*StudioPrefs.clockDy(StudioEditorActivity.this,theme)/100f;
                }
                p.setStyle(android.graphics.Paint.Style.STROKE);
                p.setColor(0xff71fbdf);p.setStrokeWidth(dp(1));
                canvas.drawCircle(cx,cy,dp(17),p);
                canvas.drawLine(cx-dp(24),cy,cx+dp(24),cy,p);
                canvas.drawLine(cx,cy-dp(24),cx,cy+dp(24),p);
            }
        }
        @Override public boolean onTouchEvent(MotionEvent e){
            switch(e.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    downX=e.getX();downY=e.getY();
                    startVertical=ThemePrefs.vertical(StudioEditorActivity.this,theme);
                    startDx=StudioPrefs.sceneDx(StudioEditorActivity.this,theme);
                    startClockX=StudioPrefs.clockDx(StudioEditorActivity.this,theme);
                    startClockY=StudioPrefs.clockDy(StudioEditorActivity.this,theme);
                    startSize=ThemePrefs.size(StudioEditorActivity.this,theme);
                    return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if(e.getPointerCount()>1)
                        lastPinch=distance(e);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if(e.getPointerCount()>1){
                        float dist=distance(e);
                        if(lastPinch>10f){
                            int size=ThemeMath.clamp(Math.round(
                                ThemePrefs.size(StudioEditorActivity.this,theme)*
                                dist/lastPinch),65,120);
                            ThemePrefs.setInt(StudioEditorActivity.this,theme,
                                "size",size);
                        }
                        lastPinch=dist;
                    }else{
                        float dx=(e.getX()-downX)/Math.max(1,getWidth());
                        float dy=(e.getY()-downY)/Math.max(1,getHeight());
                        if(editTarget==0){
                            StudioPrefs.put(StudioEditorActivity.this,theme,"scene_dx",
                                startDx+Math.round(dx*100f));
                            ThemePrefs.setInt(StudioEditorActivity.this,theme,
                                "vertical",ThemeMath.clamp(
                                    startVertical+Math.round(dy*100f),35,80));
                        }else{
                            float rr=Math.min(getWidth()*.45f,getHeight()*.255f);
                            StudioPrefs.put(StudioEditorActivity.this,theme,"clock_dx",
                                startClockX+Math.round((e.getX()-downX)/
                                    Math.max(1f,rr)*100f));
                            StudioPrefs.put(StudioEditorActivity.this,theme,"clock_dy",
                                startClockY+Math.round((e.getY()-downY)/
                                    Math.max(1f,rr)*100f));
                        }
                    }
                    invalidate();return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_POINTER_UP:
                    return true;
                default:return super.onTouchEvent(e);
            }
        }
        private float distance(MotionEvent e){
            if(e.getPointerCount()<2)return 0;
            return (float)Math.hypot(e.getX(1)-e.getX(0),e.getY(1)-e.getY(0));
        }
    }
}
