package com.creatix.neonlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.text.InputType;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/** Visual editor and local creation library; does not access device security. */
public final class ThemeDesignerActivity extends Activity {
    private static final int EXPORT=1401,IMPORT=1402;
    private int theme;
    private LinearLayout content;
    private ThemeGalleryActivity.ThemePreview preview;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        theme=ThemePrefs.selected(this);
        getWindow().setStatusBarColor(0xff030914);
        getWindow().setNavigationBarColor(0xff030914);
        build();
    }

    private int dp(int value){return MainActivity.dp(this,value);}
    private TextView label(String text,int size,int color){
        TextView t=MainActivity.text(this,text,size,color,false);
        t.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
        return t;
    }
    private void add(android.view.View view,int margin){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(margin);content.addView(view,lp);
    }
    private void section(String title){
        TextView text=label(title,16,0xff4cf3d7);
        text.setTypeface(null,1);add(text,20);
    }
    private Button button(String title){
        return MainActivity.button(this,title,0xff17293d,Color.WHITE);
    }
    private void build(){
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xff020914);
        content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(19),dp(27),dp(19),dp(33));
        scroll.addView(content);
        add(label("ATELIER NEON LOCK",27,0xfff0f9ff),0);
        add(label("Thème : "+ThemePrefs.name(theme),15,
            ThemePrefs.accent(this,theme)),7);
        preview=new ThemeGalleryActivity.ThemePreview(this,theme);
        content.addView(preview,new LinearLayout.LayoutParams(-1,dp(245)));

        section("MATIÈRES ET MÉCANISME");
        choose("Matériau",new String[]{
            "Acier original","Titane brossé","Cuivre / laiton","Carbone"
        },ThemePrefs.material(this,theme),"material");
        choose("Style des chiffres",new String[]{
            "Mécanique droit","Sérif classique","Digital technique"
        },ThemePrefs.digitStyle(this,theme),"digits");
        choose("Centre du cadran",new String[]{
            "Original","Cercle lumineux","Interface HUD","Minimal"
        },ThemePrefs.centerStyle(this,theme),"center");

        section("AMBIANCE JOUR / NUIT");
        toggle("Palette nocturne automatique",ThemePrefs.nightPalette(this,theme),
            "night_palette",false);
        Button night=button("COULEUR DE NUIT  •  "+
            String.format("#%06X",ThemePrefs.nightColor(this,theme)&0xffffff));
        night.setOnClickListener(v->changeNightColor(night));
        add(night,6);
        add(label("La palette de nuit est appliquée entre 22 h et 7 h, "+
            "sans changer ton thème.",12,0xff9aaec7),5);

        section("PROFONDEUR ET DÉMARRAGE");
        toggle("Parallaxe sur les écrans d'accueil",
            ThemePrefs.parallaxEnabled(this),"parallax",true);
        toggle("Animation d'activation (1,6 seconde)",
            ThemePrefs.startupEnabled(this),"startup_animation",true);
        add(label("La parallaxe suit les déplacements d'écran quand "+
            "le lanceur Android transmet leurs offsets.",12,0xff9aaec7),5);

        section("POSITION ACCUEIL / VERROUILLAGE");
        slider("Position écran d'accueil",35,80,
            ThemePrefs.vertical(this,theme),"vertical");
        slider("Position écran verrouillé",35,80,
            ThemePrefs.verticalForScreen(this,theme,true),"lock_vertical");
        if(Build.VERSION.SDK_INT<34){
            add(label("Sur Android antérieur à la version 14, "+
                "le système ne signale pas séparément les deux écrans. "+
                "Une position commune est alors utilisée.",12,0xffffbd74),5);
        }else{
            add(label("La position distincte fonctionne lorsque le "+
                "système fournit deux moteurs de fond séparés. "+
                "Certains constructeurs peuvent utiliser une position commune.",
                12,0xff9aaec7),5);
        }
        Button advanced=button("⚙  COULEURS, VITESSE, TAILLE ET LUMINOSITÉ");
        advanced.setOnClickListener(v->startActivity(
            new Intent(this,ThemeSettingsActivity.class)));
        add(advanced,11);

        section("MES CRÉATIONS ENREGISTRÉES");
        Button save=MainActivity.button(this,"＋  ENREGISTRER UNE VARIANTE",
            0xff41e6c7,Color.BLACK);
        save.setOnClickListener(v->askName());
        add(save,9);
        ArrayList<ThemeProfiles.Variant> saved=ThemeProfiles.list(this);
        if(saved.isEmpty())add(label("Aucune variante enregistrée.",13,
            0xff95abc4),8);
        for(ThemeProfiles.Variant item:saved){
            Button variant=button("◆  "+item.name+"  •  "+ThemePrefs.name(item.theme));
            variant.setOnClickListener(v->actions(item));
            add(variant,6);
        }
        Button export=button("↑  EXPORTER MES CRÉATIONS (JSON)");
        export.setOnClickListener(v->chooseExport());
        add(export,15);
        Button importButton=button("↓  IMPORTER DES CRÉATIONS (JSON)");
        importButton.setOnClickListener(v->chooseImport());
        add(importButton,8);
        Button diagnostics=button("◈  DIAGNOSTICS ET TEST DES 7 THÈMES");
        diagnostics.setOnClickListener(v->startActivity(
            new Intent(this,DiagnosticsActivity.class)));
        add(diagnostics,17);

        Button done=MainActivity.button(this,"✓  TERMINÉ",
            0xff42eace,Color.BLACK);
        done.setOnClickListener(v->finish());
        add(done,18);
        setContentView(scroll);
    }
    private void choose(String title,String[] names,int initial,String field){
        add(label(title,13,0xffdaeaff),9);
        Spinner spin=new Spinner(this);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item,names);
        spin.setAdapter(adapter);
        spin.setSelection(initial,false);
        spin.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(android.widget.AdapterView<?> parent,
                android.view.View view,int index,long id){
                if(index!=ThemePrefs.prefs(ThemeDesignerActivity.this)
                    .getInt("t"+theme+"_"+field,0)){
                    ThemePrefs.setInt(ThemeDesignerActivity.this,theme,field,index);
                    preview.invalidate();
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent){}
        });
        add(spin,3);
    }
    private void slider(String title,int min,int max,int value,String field){
        TextView text=label(title+"  :  "+value+" %",14,0xfff1f7ff);
        add(text,10);
        SeekBar seek=new SeekBar(this);
        seek.setMax(max-min);seek.setProgress(value-min);
        seek.setThumbTintList(ColorStateList.valueOf(0xff42edce));
        seek.setProgressTintList(ColorStateList.valueOf(0xff42edce));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar bar,int progress,boolean user){
                if(!user)return;
                int amount=min+progress;
                ThemePrefs.setInt(ThemeDesignerActivity.this,theme,field,amount);
                text.setText(title+"  :  "+amount+" %");
                preview.invalidate();
            }
            @Override public void onStartTrackingTouch(SeekBar bar){}
            @Override public void onStopTrackingTouch(SeekBar bar){}
        });
        add(seek,3);
    }
    private void toggle(String title,boolean state,String field,boolean global){
        Switch control=new Switch(this);
        control.setText(title);
        control.setTextColor(Color.WHITE);
        control.setTextSize(14);control.setChecked(state);
        control.setOnCheckedChangeListener((v,checked)->{
            if(global)ThemePrefs.setGlobal(this,field,checked);
            else ThemePrefs.setBoolean(this,theme,field,checked);
            preview.invalidate();
        });
        add(control,10);
    }
    private void changeNightColor(Button button){
        String[] options={"Bleu nuit","Violet","Cyan","Rouge sombre","Orange","Personnalisée"};
        int[] colors={0xff477aca,0xff855cd5,0xff40a8c9,0xff9c3450,0xffbf7133};
        new AlertDialog.Builder(this).setTitle("Palette nocturne")
            .setItems(options,(d,index)->{
                if(index<colors.length){
                    ThemePrefs.setInt(this,theme,"night_color",colors[index]);
                    build();
                }else{
                    EditText edit=new EditText(this);
                    edit.setSingleLine(true);edit.setHint("#477ACA");
                    edit.setInputType(InputType.TYPE_CLASS_TEXT);
                    new AlertDialog.Builder(this).setTitle("Code couleur #RRGGBB")
                        .setView(edit).setPositiveButton("OK",(dialog,which)->{
                            String value=edit.getText().toString().trim();
                            if(!value.startsWith("#"))value="#"+value;
                            if(value.matches("#[0-9A-Fa-f]{6}")){
                                ThemePrefs.setInt(this,theme,"night_color",
                                    Color.parseColor(value));
                                build();
                            }else Toast.makeText(this,
                                "Code couleur invalide",Toast.LENGTH_SHORT).show();
                        }).setNegativeButton("Annuler",null).show();
                }
            }).show();
    }
    private void askName(){
        EditText name=new EditText(this);
        name.setSingleLine(true);name.setHint("Ex : Reactor bleu glacier");
        name.setInputType(InputType.TYPE_CLASS_TEXT|
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        new AlertDialog.Builder(this).setTitle("Nom de ta création")
            .setView(name)
            .setNegativeButton("Annuler",null)
            .setPositiveButton("Enregistrer",(dialog,which)->{
                String id=ThemeProfiles.save(this,name.getText().toString(),theme);
                if(id==null)
                    Toast.makeText(this,"Maximum de 30 créations",Toast.LENGTH_LONG).show();
                else{
                    Toast.makeText(this,"Création sauvegardée",Toast.LENGTH_SHORT).show();
                    build();
                }
            }).show();
    }
    private void actions(ThemeProfiles.Variant item){
        new AlertDialog.Builder(this)
            .setTitle(item.name)
            .setItems(new String[]{"Appliquer","Renommer","Supprimer"},(d,selected)->{
                if(selected==0){
                    ThemeProfiles.apply(this,item.id);
                    theme=ThemePrefs.selected(this);
                    build();
                }else if(selected==1){
                    EditText rename=new EditText(this);
                    rename.setSingleLine(true);rename.setText(item.name);
                    new AlertDialog.Builder(this).setTitle("Renommer")
                        .setView(rename).setNegativeButton("Annuler",null)
                        .setPositiveButton("Valider",(dd,w)->{
                            ThemeProfiles.rename(this,item.id,rename.getText().toString());
                            build();
                        }).show();
                }else{
                    new AlertDialog.Builder(this)
                        .setTitle("Supprimer cette création ?")
                        .setNegativeButton("Annuler",null)
                        .setPositiveButton("Supprimer",(dd,w)->{
                            ThemeProfiles.remove(this,item.id);build();
                        }).show();
                }
            }).show();
    }
    private void chooseExport(){
        Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE,"NEON-LOCK-mes-creations.json");
        try{startActivityForResult(intent,EXPORT);}
        catch(Exception error){Toast.makeText(this,"Export indisponible",
            Toast.LENGTH_LONG).show();}
    }
    private void chooseImport(){
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        try{startActivityForResult(intent,IMPORT);}
        catch(Exception error){Toast.makeText(this,"Import indisponible",
            Toast.LENGTH_LONG).show();}
    }
    @Override protected void onActivityResult(int code,int result,Intent data){
        super.onActivityResult(code,result,data);
        if(result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        try{
            if(code==EXPORT){
                try(OutputStream out=getContentResolver().openOutputStream(uri)){
                    if(out==null)throw new Exception("Fichier non accessible");
                    byte[] bytes=ThemeProfiles.exportAll(this)
                        .getBytes(StandardCharsets.UTF_8);
                    out.write(bytes);out.flush();
                }
                Toast.makeText(this,"Créations exportées",Toast.LENGTH_LONG).show();
            }else if(code==IMPORT){
                ByteArrayOutputStream buffer=new ByteArrayOutputStream();
                try(InputStream stream=getContentResolver().openInputStream(uri)){
                    if(stream==null)throw new Exception("Fichier non accessible");
                    byte[] block=new byte[4096];int amount;
                    while((amount=stream.read(block))!=-1){
                        if(buffer.size()+amount>200000)
                            throw new Exception("Fichier trop volumineux");
                        buffer.write(block,0,amount);
                    }
                }
                int count=ThemeProfiles.importAll(this,
                    buffer.toString("UTF-8"));
                Toast.makeText(this,count+" création(s) importée(s)",
                    Toast.LENGTH_LONG).show();
                build();
            }
        }catch(Exception error){
            new AlertDialog.Builder(this).setTitle("Opération impossible")
                .setMessage(error.getMessage())
                .setPositiveButton("OK",null).show();
        }
    }
    @Override protected void onResume(){super.onResume();if(preview!=null)preview.invalidate();}
}
