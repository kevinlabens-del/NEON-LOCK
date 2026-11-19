package com.creatix.neonlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.content.Intent;

/** Independent settings for each 3D theme; no global color override. */
public final class ThemeSettingsActivity extends Activity {
    private LinearLayout content;
    private final int[] colors={
        0xffffab2b,0xff27dfff,0xffffcc66,0xffff394b,0xffd59d5a,
        0xffb465ff,0xff4aeaff,0xff2bffb3,0xffe9efff,0xffff4ea8,0xff67ff5c
    };
    private final String[] labels={
        "Orange","Cyan","Or","Rouge","Cuivre","Violet",
        "Glacier","Vert néon","Blanc","Rose","Vert citron",
        "Personnalisé (#RRGGBB)"
    };
    private int theme;

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        theme=ThemePrefs.selected(this);
        getWindow().setStatusBarColor(0xff040713);
        getWindow().setNavigationBarColor(0xff040713);
        build();
    }

    private int dp(int value){return MainActivity.dp(this,value);}
    private TextView label(String text,int size,int color,boolean bold){
        TextView v=MainActivity.text(this,text,size,color,bold);
        v.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
        return v;
    }
    private void add(View view,int margin){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(margin);
        content.addView(view,lp);
    }
    private void section(String text){
        add(label(text,15,0xff53eaca,true),20);
    }
    private Button button(String text){
        return MainActivity.button(this,text,0xff1a293c,0xfff0f8ff);
    }
    private void build(){
        ScrollView scroll=new ScrollView(this);
        scroll.setBackgroundColor(0xff050a14);
        content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(19),dp(22),dp(19),dp(30));
        scroll.addView(content);
        add(label("PERSONNALISER LE THÈME",23,0xfff2f8ff,true),1);
        add(label(ThemePrefs.name(theme),17,ThemePrefs.accent(this,theme),true),9);
        ThemeGalleryActivity.ThemePreview preview=
            new ThemeGalleryActivity.ThemePreview(this,theme);
        LinearLayout.LayoutParams pr=new LinearLayout.LayoutParams(-1,dp(250));
        pr.topMargin=dp(12);
        content.addView(preview,pr);

        section("COULEUR DU THÈME");
        Button colorButton=button("●  COULEUR ACTUELLE : "+formatColor());
        colorButton.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Couleur de "+ThemePrefs.name(theme))
            .setItems(labels,(dialog,choice)->{
                if(choice>=colors.length)customColor(colorButton);
                else{
                    ThemePrefs.setAccent(this,theme,colors[choice]);
                    colorButton.setText("●  COULEUR ACTUELLE : "+formatColor());
                    preview.invalidate();
                }
            }).setNegativeButton("Annuler",null).show());
        add(colorButton,8);

        section("AJUSTER LES EFFETS 3D");
        addSlider("Intensité lumineuse",50,255,ThemePrefs.glow(this,theme),"glow"," %");
        addSlider("Taille du cadran",65,120,ThemePrefs.size(this,theme),"size"," %");
        addSlider("Vitesse de rotation",20,200,ThemePrefs.speed(this,theme),"speed"," %");
        addSlider("Position verticale",35,80,ThemePrefs.vertical(this,theme),"vertical"," %");

        addToggle("Inverser le sens de rotation",
            ThemePrefs.reverse(this,theme),checked->{
                ThemePrefs.setBoolean(this,theme,"reverse",checked);
                preview.invalidate();
            });
        addToggle("Luminosité réduite la nuit (22 h–7 h)",
            ThemePrefs.adaptive(this,theme),checked->{
                ThemePrefs.setBoolean(this,theme,"adaptive",checked);
                preview.invalidate();
            });

        section("ÉCONOMIE DE BATTERIE");
        addToggle("Mode économie (15 images/seconde)",
            ThemePrefs.batterySaver(this),checked->{
                ThemePrefs.setBatterySaver(this,checked);
            });

        section("ATELIER ET CRÉATIONS PERSONNALISÉES");
        Button workshop=MainActivity.button(this,
            "✦  MATIÈRES, CHIFFRES, PROFILS ET PALETTES JOUR/NUIT",
            0xff21364f,0xfff0f7ff);
        workshop.setOnClickListener(v->startActivity(
            new Intent(this,ThemeDesignerActivity.class)));
        add(workshop,8);

        section("CHANGEMENT AUTOMATIQUE DES THÈMES");
        addToggle("Changer de thème automatiquement",
            ThemePrefs.autoChange(this),checked->{
                ThemePrefs.setAutoChange(this,checked);
            });
        Button interval=button("PÉRIODE : toutes les "+
            ThemePrefs.intervalHours(this)+" heure(s)");
        interval.setOnClickListener(v->{
            String[] options={"1 heure","3 heures","6 heures","12 heures","24 heures"};
            int[] hours={1,3,6,12,24};
            new AlertDialog.Builder(this)
                .setTitle("Rotation automatique")
                .setItems(options,(dialog,index)->{
                    ThemePrefs.setIntervalHours(this,hours[index]);
                    interval.setText("PÉRIODE : toutes les "+
                        ThemePrefs.intervalHours(this)+" heure(s)");
                }).show();
        });
        add(interval,7);

        if(theme==ThemePrefs.VAULT){
            section("RÉGLAGES HISTORIQUES DU COFFRE-FORT");
            Button legacy=button("RÉGLAGES AVANCÉS VAULT ORIGINAL");
            legacy.setOnClickListener(v->startActivity(
                new Intent(this,SettingsActivity.class)));
            add(legacy,7);
        }

        Button reset=button("RÉTABLIR LES VALEURS DE CE THÈME");
        reset.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("Réinitialiser "+ThemePrefs.name(theme)+" ?")
            .setMessage("Seuls les réglages de ce thème seront réinitialisés.")
            .setNegativeButton("Annuler",null)
            .setPositiveButton("Réinitialiser",(dialog,which)->{
                ThemePrefs.reset(theme,this);
                build();
            }).show());
        add(reset,23);
        Button done=MainActivity.button(this,"✓  TERMINÉ",
            0xff36ebc7,0xff031014);
        done.setOnClickListener(v->finish());
        add(done,11);
        setContentView(scroll);
    }
    private String formatColor(){
        return String.format("#%06X",ThemePrefs.accent(this,theme)&0xffffff);
    }

    private interface SwitchAction{void change(boolean value);}
    private void addToggle(String text,boolean enabled,SwitchAction action){
        Switch box=new Switch(this);
        box.setText(text);
        box.setTextColor(0xffeef5ff);
        box.setTextSize(14);
        box.setPadding(0,dp(5),0,dp(5));
        box.setChecked(enabled);
        box.setOnCheckedChangeListener((v,checked)->action.change(checked));
        add(box,10);
    }
    private void addSlider(String title,int min,int max,int initial,
                           String key,String unit){
        TextView caption=label(title+"  •  "+initial+unit,14,0xffe3f0fc,true);
        add(caption,12);
        SeekBar bar=new SeekBar(this);
        bar.setMax(max-min);
        bar.setProgress(Math.max(0,Math.min(max-min,initial-min)));
        int accent=ThemePrefs.accent(this,theme);
        bar.setProgressTintList(ColorStateList.valueOf(accent));
        bar.setThumbTintList(ColorStateList.valueOf(accent));
        add(bar,3);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar bar,int progress,boolean user){
                if(!user)return;
                int amount=min+progress;
                ThemePrefs.setInt(ThemeSettingsActivity.this,theme,key,amount);
                caption.setText(title+"  •  "+amount+unit);
            }
            @Override public void onStartTrackingTouch(SeekBar bar){}
            @Override public void onStopTrackingTouch(SeekBar bar){}
        });
    }
    private void customColor(Button button){
        EditText input=new EditText(this);
        input.setHint("#2BFFB3");
        input.setSingleLine(true);
        input.setText(formatColor());
        input.setInputType(InputType.TYPE_CLASS_TEXT|
            InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        LinearLayout holder=new LinearLayout(this);
        holder.setPadding(dp(18),0,dp(18),0);
        holder.addView(input);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle("Couleur personnalisée")
            .setView(holder).setNegativeButton("Annuler",null)
            .setPositiveButton("Valider",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(w->{
                String hex=input.getText().toString().trim();
                if(!hex.startsWith("#"))hex="#"+hex;
                if(!hex.matches("#[0-9A-Fa-f]{6}")){
                    input.setError("Saisis 6 chiffres hexadécimaux");
                    return;
                }
                ThemePrefs.setAccent(this,theme,Color.parseColor(hex));
                button.setText("●  COULEUR ACTUELLE : "+formatColor());
                dialog.dismiss();
            }));
        dialog.show();
    }
}
