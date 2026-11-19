package com.creatix.neonlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

public final class SettingsActivity extends Activity {
    private LinearLayout content;
    private MainActivity.ClockView preview;
    private TextView positionText;
    private final int[] palette={
        0xff2bffb3,0xffffab2b,0xffe8f1fe,0xff34d7ff,
        0xffff5d9e,0xffff536b,0xffa677ff,0xffffe66b,
        0xffb2c7e3,0xff62ff4f,0xff0099ff,0xff131c2b
    };
    private final String[] names={
        "Vert néon","Orange","Blanc","Bleu électrique",
        "Rose","Rouge","Violet","Jaune",
        "Bleu gris","Vert citron","Bleu intense","Bleu nuit",
        "Personnaliser (#RRGGBB)…"
    };
    @Override public void onCreate(Bundle state){
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
        MainActivity.add(content,label("PERSONNALISATION COFFRE-FORT",24,MainActivity.WHITE,true),this,0);
        MainActivity.add(content,label("Porte blindée, boulons, charnières et molette en acier. Le cadran 0–6 tourne en 60 secondes, avec l'heure au centre.",
            14,MainActivity.MUTED,false),this,6);
        preview=new MainActivity.ClockView(this,true);
        LinearLayout.LayoutParams previewLp=new LinearLayout.LayoutParams(-1,MainActivity.dp(this,325));
        previewLp.topMargin=MainActivity.dp(this,10);
        content.addView(preview,previewLp);

        section("AFFICHAGE");
        toggle("Afficher « NEON LOCK »",LockPrefs.SHOW_TITLE,
            LockPrefs.get(this).showTitle);
        toggle("Afficher la date",LockPrefs.SHOW_DATE,
            LockPrefs.get(this).showDate);
        section("COULEURS");
        chooseColor("Couleur du titre",LockPrefs.TITLE_COLOR);
        chooseColor("Couleur de la date",LockPrefs.DATE_COLOR);
        chooseColor("Éclairage néon des arcs et du repère",LockPrefs.RING_COLOR);
        chooseColor("Couleur des chiffres métalliques",LockPrefs.NUMBER_COLOR);
        chooseColor("Couleur de l'heure",LockPrefs.CLOCK_COLOR);
        section("POSITION SUR L'ÉCRAN");
        positionText=label("",15,MainActivity.WHITE,true);
        MainActivity.add(content,positionText,this,6);
        SeekBar position=new SeekBar(this);
        position.setMax(35);
        position.setProgress(LockPrefs.get(this).verticalPercent-40);
        position.setProgressTintList(ColorStateList.valueOf(MainActivity.GREEN));
        position.setThumbTintList(ColorStateList.valueOf(MainActivity.GREEN));
        MainActivity.add(content,position,this,5);
        position.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar bar,int progress,boolean fromUser){
                if(!fromUser)return;
                LockPrefs.prefs(SettingsActivity.this).edit()
                    .putInt(LockPrefs.DIAL_VERTICAL,40+progress).apply();
                refresh();
            }
            @Override public void onStartTrackingTouch(SeekBar bar){}
            @Override public void onStopTrackingTouch(SeekBar bar){}
        });
        MainActivity.add(content,label("Déplace le cadran pour éviter l'horloge intégrée d'Android sur l'écran verrouillé.",
            13,MainActivity.MUTED,false),this,7);
        Button reset=MainActivity.button(this,"RÉTABLIR L'APPARENCE",
            MainActivity.PANEL,MainActivity.WHITE);
        reset.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Réinitialiser les couleurs ?")
            .setMessage("Le fond animé restera disponible.")
            .setNegativeButton("Annuler",null)
            .setPositiveButton("Réinitialiser",(d,w)->{
                LockPrefs.prefs(this).edit().clear().apply();
                recreate();
            }).show());
        MainActivity.add(content,reset,this,16);
        MainActivity.add(content,label("Pas de code NEON ni de service d'accessibilité. "+
            "Le déverrouillage de ton téléphone est contrôlé dans les paramètres Android.",
            13,MainActivity.MUTED,false),this,16);
        Button done=MainActivity.button(this,"TERMINÉ",MainActivity.GREEN,Color.BLACK);
        done.setOnClickListener(v->finish());
        MainActivity.add(content,done,this,14);
        setContentView(scroll);
        refresh();
    }
    @Override protected void onResume(){
        super.onResume();
        refresh();
    }
    private TextView label(String s,int size,int color,boolean bold){
        TextView t=MainActivity.text(this,s,size,color,bold);
        t.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        return t;
    }
    private void section(String s){
        MainActivity.add(content,label(s,15,MainActivity.GREEN,true),this,18);
    }
    private void toggle(String title,String key,boolean initial){
        Switch control=new Switch(this);
        control.setText(title);
        control.setTextColor(MainActivity.WHITE);
        control.setTextSize(16);
        control.setPadding(0,MainActivity.dp(this,6),0,MainActivity.dp(this,6));
        control.setChecked(initial);
        control.setOnCheckedChangeListener((v,checked)->{
            LockPrefs.prefs(this).edit().putBoolean(key,checked).apply();
            refresh();
        });
        MainActivity.add(content,control,this,7);
    }
    private void chooseColor(String title,String key){
        Button button=MainActivity.button(this,title,MainActivity.PANEL,MainActivity.WHITE);
        refreshColor(button,title,key);
        button.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(names,(dialog,choice)->{
                if(choice==palette.length)customColor(button,title,key);
                else saveColor(button,title,key,palette[choice]);
            })
            .setNegativeButton("Annuler",null).show());
        MainActivity.add(content,button,this,7);
    }
    private void refreshColor(Button b,String label,String key){
        LockPrefs.Style s=LockPrefs.get(this);
        int value;
        switch(key){
            case LockPrefs.TITLE_COLOR:value=s.titleColor;break;
            case LockPrefs.DATE_COLOR:value=s.dateColor;break;
            case LockPrefs.RING_COLOR:value=s.ringColor;break;
            case LockPrefs.NUMBER_COLOR:value=s.numberColor;break;
            case LockPrefs.CLOCK_COLOR:value=s.clockColor;break;
            default:value=MainActivity.WHITE;
        }
        b.setText("●  "+label+"    "+String.format("#%06X",value&0xffffff));
    }
    private void saveColor(Button button,String label,String key,int value){
        LockPrefs.prefs(this).edit().putInt(key,value|0xff000000).apply();
        refreshColor(button,label,key);
        refresh();
    }
    private void customColor(Button button,String label,String key){
        EditText hex=new EditText(this);
        hex.setSingleLine(true);
        hex.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        hex.setHint("#2BFFB3");
        LinearLayout holder=new LinearLayout(this);
        holder.setPadding(MainActivity.dp(this,20),0,MainActivity.dp(this,20),0);
        holder.addView(hex);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle("Couleur personnalisée")
            .setView(holder)
            .setNegativeButton("Annuler",null)
            .setPositiveButton("Valider",null)
            .create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                String input=hex.getText().toString().trim();
                if(!input.startsWith("#"))input="#"+input;
                if(!input.matches("#[0-9A-Fa-f]{6}")){
                    hex.setError("Saisis 6 caractères hexadécimaux");
                    return;
                }
                saveColor(button,label,key,Color.parseColor(input));
                dialog.dismiss();
            }));
        dialog.show();
    }
    private void refresh(){
        if(preview!=null)preview.invalidate();
        if(positionText!=null)
            positionText.setText("Position du cadran : "+LockPrefs.get(this).verticalPercent+" %");
    }
}
