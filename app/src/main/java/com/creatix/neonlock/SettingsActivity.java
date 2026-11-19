package com.creatix.neonlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.content.Intent;
import android.content.ComponentName;
import android.provider.Settings;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    private LinearLayout content;
    private MainActivity.Wheel preview;
    private TextView dialPosition;
    private TextView accessibilityStatus;
    private final int[] palette = {
        0xff2bffb3, 0xffffab2b, 0xffe8f1fe, 0xff34d7ff,
        0xffff5d9e, 0xffff536b, 0xffa677ff, 0xffffe66b,
        0xffb2c7e3, 0xff62ff4f, 0xff0099ff, 0xff131c2b
    };
    private final String[] names = {
        "Vert néon", "Orange", "Blanc", "Bleu électrique",
        "Rose", "Rouge", "Violet", "Jaune",
        "Bleu gris", "Vert citron", "Bleu intense", "Bleu nuit",
        "Personnaliser (#RRGGBB)…"
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(MainActivity.BG);
        getWindow().setNavigationBarColor(MainActivity.BG);
        ScrollView scroll=new ScrollView(this);
        scroll.setBackgroundColor(MainActivity.BG);
        content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(MainActivity.dp(this,20),MainActivity.dp(this,24),
            MainActivity.dp(this,20),MainActivity.dp(this,28));
        scroll.addView(content);
        MainActivity.add(content,label("PERSONNALISATION",25,MainActivity.WHITE,true),this,0);
        MainActivity.add(content,label("Modifie ton fond animé en direct. Les réglages sont mémorisés.",13,MainActivity.MUTED,false),this,5);

        preview=new MainActivity.Wheel(this,false);
        preview.setWallpaperPreview(true);
        LinearLayout.LayoutParams previewParams=new LinearLayout.LayoutParams(-1,MainActivity.dp(this,330));
        previewParams.setMargins(0,MainActivity.dp(this,10),0,MainActivity.dp(this,2));
        content.addView(preview,previewParams);

        section("CADRAN AUTOMATIQUE — SANS ROOT");
        MainActivity.add(content,label(
            "Mode expérimental : NEON LOCK tente d'afficher le cadran quand tu allumes l'écran verrouillé. " +
            "Ton code Android reste obligatoire. Un bouton permet toujours de revenir à Android.",
            14,MainActivity.MUTED,false),this,9);
        toggle("Afficher mon cadran au réveil",LockPrefs.AUTO_DIAL,
            LockPrefs.prefs(this).getBoolean(LockPrefs.AUTO_DIAL,false));
        accessibilityStatus=label("",14,MainActivity.WHITE,false);
        MainActivity.add(content,accessibilityStatus,this,9);
        Button authorize=MainActivity.button(this,"OUVRIR LES PARAMÈTRES D’ACCESSIBILITÉ",
            MainActivity.GREEN,Color.BLACK);
        authorize.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (Exception error) {
                Toast.makeText(this,"Réglages d’accessibilité indisponibles.",Toast.LENGTH_LONG).show();
            }
        });
        MainActivity.add(content,authorize,this,8);
        MainActivity.add(content,label(
            "L'autorisation est facultative et sensible. Ce service ne lit pas le contenu " +
            "des autres applications et ne pilote pas leurs actions. Pour arrêter le cadran : " +
            "désactive l'interrupteur ci-dessus ou le service d'accessibilité.",
            12,MainActivity.MUTED,false),this,8);

        section("AFFICHAGE DU TEXTE");
        toggle("Afficher « NEON LOCK »",LockPrefs.SHOW_TITLE,LockPrefs.get(this).showTitle);
        toggle("Afficher la date",LockPrefs.SHOW_DATE,LockPrefs.get(this).showDate);

        section("COULEURS");
        chooseColor("Couleur du titre",LockPrefs.TITLE_COLOR);
        chooseColor("Couleur de la date",LockPrefs.DATE_COLOR);
        chooseColor("Couleur du cercle",LockPrefs.RING_COLOR);
        chooseColor("Couleur des chiffres",LockPrefs.NUMBER_COLOR);
        chooseColor("Couleur de l'horloge",LockPrefs.CLOCK_COLOR);

        section("POSITION DU CADRAN");
        dialPosition=label("",15,MainActivity.WHITE,true);
        MainActivity.add(content,dialPosition,this,7);
        SeekBar slider=new SeekBar(this);
        slider.setMax(17);
        slider.setProgress(LockPrefs.get(this).verticalPercent-58);
        slider.setProgressTintList(ColorStateList.valueOf(MainActivity.GREEN));
        slider.setThumbTintList(ColorStateList.valueOf(MainActivity.GREEN));
        MainActivity.add(content,slider,this,4);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar bar,int progress,boolean fromUser){
                int percent=58+progress;
                LockPrefs.prefs(SettingsActivity.this).edit().putInt(LockPrefs.DIAL_VERTICAL,percent).apply();
                refresh();
            }
            @Override public void onStartTrackingTouch(SeekBar bar){}
            @Override public void onStopTrackingTouch(SeekBar bar){}
        });
        MainActivity.add(content,label("Glisse vers la droite pour descendre le cercle. La position basse (69 %) évite davantage le texte natif d’Android.",13,MainActivity.MUTED,false),this,6);

        Button reset=MainActivity.button(this,"RÉTABLIR LES COULEURS ET L'AFFICHAGE",MainActivity.PANEL,MainActivity.WHITE);
        reset.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle("Rétablir l'apparence ?")
            .setMessage("Cela ne supprimera pas ton code visuel.")
            .setNegativeButton("Annuler",null)
            .setPositiveButton("Rétablir",(dialog,which)->{
                LockPrefs.prefs(this).edit().clear().apply();
                recreate();
            }).show());
        MainActivity.add(content,reset,this,18);

        MainActivity.add(content,label("Ces options modifient les éléments NEON LOCK, pas l'heure ou la date du système Android. Le véritable code PIN Android reste actif.",12,MainActivity.MUTED,false),this,15);
        Button done=MainActivity.button(this,"TERMINÉ",MainActivity.GREEN,Color.BLACK);
        done.setOnClickListener(v -> finish());
        MainActivity.add(content,done,this,14);

        refresh();
        setContentView(scroll);
    }

    @Override protected void onResume(){
        super.onResume();
        refresh();
    }

    private boolean accessibilityEnabled() {
        try {
            AccessibilityManager am=(AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
            String id=new ComponentName(this,NeonLockAccessibilityService.class).flattenToString();
            if(am!=null){
                for(AccessibilityServiceInfo info:am.getEnabledAccessibilityServiceList(
                    AccessibilityServiceInfo.FEEDBACK_ALL_MASK)){
                    if(id.equals(info.getId()))return true;
                }
            }
        }catch(Exception ignored){}
        return false;
    }

    private TextView label(String text,int size,int color,boolean bold){
        TextView t=MainActivity.text(this,text,size,color,bold);
        t.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        return t;
    }
    private void section(String title){
        MainActivity.add(content,label(title,15,MainActivity.GREEN,true),this,20);
    }
    private void toggle(String label,String key,boolean checked){
        Switch sw=new Switch(this);
        sw.setText(label);
        sw.setTextColor(MainActivity.WHITE);
        sw.setTextSize(16);
        sw.setPadding(0,MainActivity.dp(this,6),0,MainActivity.dp(this,6));
        sw.setChecked(checked);
        sw.setOnCheckedChangeListener((b,isChecked)->{
            LockPrefs.prefs(this).edit().putBoolean(key,isChecked).apply();
            if(LockPrefs.AUTO_DIAL.equals(key) && isChecked &&
                 MainActivity.Credentials.length(this)==0){
                Toast.makeText(this,"Configure d'abord ton code visuel dans NEON LOCK.",Toast.LENGTH_LONG).show();
            }
            refresh();
        });
        MainActivity.add(content,sw,this,10);
    }
    private void chooseColor(String label,String key){
        Button b=MainActivity.button(this,label,MainActivity.PANEL,MainActivity.WHITE);
        refreshColor(b,label,key);
        b.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle(label)
            .setItems(names,(dialog,choice)->{
                if(choice==palette.length)customColor(b,label,key);
                else saveColor(b,label,key,palette[choice]);
            })
            .setNegativeButton("Annuler",null)
            .show());
        MainActivity.add(content,b,this,7);
    }
    private void refreshColor(Button b,String label,String key){
        LockPrefs.Style style=LockPrefs.get(this);
        int col;
        switch(key){
            case LockPrefs.TITLE_COLOR: col=style.titleColor;break;
            case LockPrefs.DATE_COLOR: col=style.dateColor;break;
            case LockPrefs.RING_COLOR: col=style.ringColor;break;
            case LockPrefs.NUMBER_COLOR: col=style.numberColor;break;
            case LockPrefs.CLOCK_COLOR: col=style.clockColor;break;
            default: col=MainActivity.WHITE;
        }
        b.setText("●  "+label+"     "+String.format("#%06X",col&0xffffff));
        b.setTextColor(MainActivity.WHITE);
    }
    private void saveColor(Button b,String label,String key,int value){
        LockPrefs.prefs(this).edit().putInt(key,value|0xff000000).apply();
        refreshColor(b,label,key);
        refresh();
    }
    private void customColor(Button button,String label,String key){
        EditText hex=new EditText(this);
        hex.setSingleLine(true);
        hex.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        hex.setHint("#2BFFB3");
        hex.setTextColor(MainActivity.WHITE);
        LinearLayout holder=new LinearLayout(this);
        holder.setPadding(MainActivity.dp(this,20),0,MainActivity.dp(this,20),0);
        holder.addView(hex);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle("Couleur personnalisée")
            .setMessage("Saisis un code hexadécimal à 6 caractères, par exemple #FF6600.")
            .setView(holder)
            .setNegativeButton("Annuler",null)
            .setPositiveButton("Valider",null)
            .create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w -> {
                String s=hex.getText().toString().trim();
                if(!s.startsWith("#"))s="#"+s;
                if(!s.matches("#[0-9A-Fa-f]{6}")) {
                    hex.setError("Format attendu : #RRGGBB"); return;
                }
                saveColor(button,label,key,Color.parseColor(s));
                dialog.dismiss();
            }));
        dialog.show();
    }
    private void refresh(){
        if(preview!=null)preview.invalidate();
        if(dialPosition!=null){
            dialPosition.setText("Hauteur du cadran : "+LockPrefs.get(this).verticalPercent+" % de l'écran");
        }
        if(accessibilityStatus!=null){
            boolean enabled=accessibilityEnabled();
            boolean selected=LockPrefs.prefs(this).getBoolean(LockPrefs.AUTO_DIAL,false);
            String access=enabled?"Service Android autorisé ✓":"Service Android non autorisé";
            String pin=MainActivity.Credentials.length(this)>0?"Code visuel disponible":"Code visuel à configurer";
            accessibilityStatus.setText(access+"\n"+pin+"\nMode automatique : "+(selected?"activé":"désactivé"));
            accessibilityStatus.setTextColor(enabled&&selected&&MainActivity.Credentials.length(this)>0
                ?MainActivity.GREEN:MainActivity.MUTED);
        }
    }
}
