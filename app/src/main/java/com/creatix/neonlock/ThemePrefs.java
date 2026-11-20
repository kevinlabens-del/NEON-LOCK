package com.creatix.neonlock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import java.util.Calendar;

/** Local-only settings. Each visual theme owns its accent, glow, size, speed and position. */
public final class ThemePrefs {
    public static final int VAULT=0, CYBER=1, GOLD=2, RED=3,
        STEAMPUNK=4, GALAXY=5, ARC=6, NEON_GRID=7, FROST=8,\n        LAVA=9, SHADOW=10, OCEAN=11, COUNT=12;
    public static final String[] NAMES={
        "VAULT ORIGINAL","CYBER HOLOGRAM","ROYAL GOLD","RED REACTOR",
        "STEAMPUNK","GALAXY PULSE","ARC REACTOR",\n        "NEON GRID","FROST CORE","LAVA CORE","SHADOW TECH","OCEAN PULSE"
    };
    public static final String[] SUBTITLES={
        "Le coffre-fort original","Hologramme futuriste","Horlogerie de luxe",
        "Réacteur rouge","Engrenages et cuivre","Énergie cosmique",
        "Cœur énergétique bleu",\n        "Terminal néon et grille perspective","Cristaux et glace fractale",\n        "Roche noire et magma","Interface furtive industrielle",\n        "Hublot et profondeurs sous-marines"
    };
    private static final int[] COLORS={
        0xffffab2b,0xff26dfff,0xffffc35b,0xffff344b,
        0xffd99b4e,0xffb466ff,0xff57eaff,\n        0xff50e9ff,0xffb0eaff,0xffff6938,0xff89ebc0,0xff4be6e0
    };
    private static final String PREFS="neon_theme_preferences";
    private ThemePrefs(){}

    public static SharedPreferences prefs(Context c){
        return c.getApplicationContext().getSharedPreferences(PREFS,Context.MODE_PRIVATE);
    }
    public static int safe(int index){return Math.max(0,Math.min(COUNT-1,index));}
    public static String name(int index){return NAMES[safe(index)];}
    public static int defaultColor(int index){return COLORS[safe(index)];}
    private static String key(int index,String field){return "t"+safe(index)+"_"+field;}

    /** Direct selection is retained even if auto-changing is temporarily active. */
    public static int selected(Context c){return safe(prefs(c).getInt("selected",0));}
    public static void select(Context c,int index){
        int v=safe(index);
        prefs(c).edit().putInt("selected",v)
            .putInt("auto_anchor_theme",v)
            .putLong("auto_anchor_time",System.currentTimeMillis()).apply();
    }
    public static boolean autoChange(Context c){return prefs(c).getBoolean("auto_change",false);}
    public static boolean batterySaver(Context c){return prefs(c).getBoolean("battery_saver",false);}
    public static void setBatterySaver(Context c,boolean enabled){
        prefs(c).edit().putBoolean("battery_saver",enabled).apply();
    }
    public static void setAutoChange(Context c,boolean enabled){
        prefs(c).edit().putBoolean("auto_change",enabled)
            .putInt("auto_anchor_theme",selected(c))
            .putLong("auto_anchor_time",System.currentTimeMillis()).apply();
    }
    public static int intervalHours(Context c){
        return Math.max(1,Math.min(24,prefs(c).getInt("auto_interval",6)));
    }
    public static void setIntervalHours(Context c,int h){
        prefs(c).edit().putInt("auto_interval",Math.max(1,Math.min(24,h)))
            .putInt("auto_anchor_theme",selected(c))
            .putLong("auto_anchor_time",System.currentTimeMillis()).apply();
    }
    public static int active(Context c){
        SharedPreferences p=prefs(c);
        if(!p.getBoolean("auto_change",false))return selected(c);
        long anchor=p.getLong("auto_anchor_time",System.currentTimeMillis());
        long window=intervalHours(c)*3600000L;
        long cycles=Math.max(0,(System.currentTimeMillis()-anchor)/window);
        return (int)((safe(p.getInt("auto_anchor_theme",selected(c)))+cycles)%COUNT);
    }
    public static int accent(Context c,int index){
        return prefs(c).getInt(key(index,"accent"),defaultColor(index));
    }
    public static int material(Context c,int id){
        return ThemeMath.clamp(prefs(c).getInt(key(id,"material"),0),0,3);
    }
    public static int digitStyle(Context c,int id){
        return ThemeMath.clamp(prefs(c).getInt(key(id,"digits"),0),0,2);
    }
    public static int centerStyle(Context c,int id){
        return ThemeMath.clamp(prefs(c).getInt(key(id,"center"),0),0,3);
    }
    public static boolean nightPalette(Context c,int id){
        return prefs(c).getBoolean(key(id,"night_palette"),false);
    }
    public static int nightColor(Context c,int id){
        return prefs(c).getInt(key(id,"night_color"),0xff477aca);
    }
    public static int effectiveAccent(Context c,int index){
        int original=accent(c,index);
        if(!nightPalette(c,index))return original;
        int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(!ThemeMath.isNight(hour))return original;
        int target=nightColor(c,index);
        return Color.rgb(
            (Color.red(original)+Color.red(target))/2,
            (Color.green(original)+Color.green(target))/2,
            (Color.blue(original)+Color.blue(target))/2);
    }
    /** For devices where Android exposes separate home and lock wallpaper engines (API 34+). */
    public static int verticalForScreen(Context c,int index,boolean lock){
        if(!lock)return vertical(c,index);
        return ThemeMath.clamp(prefs(c).getInt(key(index,"lock_vertical"),
            vertical(c,index)),35,80);
    }
    public static boolean parallaxEnabled(Context c){
        return prefs(c).getBoolean("parallax",true);
    }
    public static boolean startupEnabled(Context c){
        return prefs(c).getBoolean("startup_animation",true);
    }
    public static int targetFps(Context c,boolean powerSave){
        if(powerSave)return 12;
        return batterySaver(c)?15:30;
    }
    public static void setGlobal(Context c,String key,boolean value){
        prefs(c).edit().putBoolean(key,value).apply();
    }
    public static void setAccent(Context c,int index,int color){
        prefs(c).edit().putInt(key(index,"accent"),color|0xff000000).apply();
        // The old Vault settings remain authoritative for the original bitmap.
        if(safe(index)==VAULT)
            LockPrefs.prefs(c).edit().putInt(LockPrefs.RING_COLOR,color|0xff000000).apply();
    }
    public static int glow(Context c,int index){
        return Math.max(50,Math.min(255,prefs(c).getInt(key(index,"glow"),190)));
    }
    public static int size(Context c,int index){
        return Math.max(65,Math.min(120,prefs(c).getInt(key(index,"size"),100)));
    }
    public static int speed(Context c,int index){
        return Math.max(20,Math.min(200,prefs(c).getInt(key(index,"speed"),100)));
    }
    public static int vertical(Context c,int index){
        int defaultValue=index==VAULT?LockPrefs.get(c).verticalPercent:69;
        return Math.max(35,Math.min(80,prefs(c).getInt(key(index,"vertical"),defaultValue)));
    }
    public static boolean adaptive(Context c,int index){
        return prefs(c).getBoolean(key(index,"adaptive"),false);
    }
    public static boolean reverse(Context c,int index){
        return prefs(c).getBoolean(key(index,"reverse"),false);
    }
    public static void setInt(Context c,int index,String field,int value){
        prefs(c).edit().putInt(key(index,field),value).apply();
        if(index==VAULT && field.equals("vertical"))
            LockPrefs.prefs(c).edit().putInt(LockPrefs.DIAL_VERTICAL,value).apply();
        if(index==VAULT && field.equals("glow"))
            LockPrefs.prefs(c).edit().putInt(LockPrefs.NEON_INTENSITY,value).apply();
        if(index==VAULT && field.equals("reverse"))
            LockPrefs.prefs(c).edit().putBoolean(LockPrefs.REVERSE_ROTATION,value!=0).apply();
    }
    public static void setBoolean(Context c,int index,String field,boolean value){
        prefs(c).edit().putBoolean(key(index,field),value).apply();
        if(index==VAULT && field.equals("reverse"))
            LockPrefs.prefs(c).edit().putBoolean(LockPrefs.REVERSE_ROTATION,value).apply();
    }
    public static float glowFactor(Context c,int index){
        float g=glow(c,index)/200f;
        if(adaptive(c,index)){
            int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if(hour>=22 || hour<7)g*=.66f;
        }
        return Math.max(.20f,Math.min(1.45f,g));
    }
    public static void reset(int index,Context c){
        SharedPreferences.Editor edit=prefs(c).edit();
        for(String field:new String[]{"accent","glow","size","speed","vertical","lock_vertical",
            "adaptive","reverse","material","digits","center","night_palette","night_color"})
            edit.remove(key(index,field));
        edit.apply();
    }
}
