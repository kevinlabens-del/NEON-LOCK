package com.creatix.neonlock;

import android.content.Context;
import android.graphics.Color;

/**
 * Local edit layer for v1.5.0. Uses the existing SharedPreferences store:
 * theme 0-11 IDs, saved profiles and historical selections are never reset.
 */
public final class StudioPrefs {
    public static final int CALM=0, NORMAL=1, INTENSE=2, ECO=3;
    public static final String[] MOTIONS={"Calme","Normal","Intense","Économie"};
    public static final String[] CLOCKS={
        "Numérique premium","Mécanique","Holographique","Minimaliste"
    };
    private StudioPrefs(){}
    private static String k(int theme,String item){return "studio_"+ThemePrefs.safe(theme)+"_"+item;}
    public static int clockStyle(Context c,int t){
        return ThemeMath.clamp(ThemePrefs.prefs(c).getInt(k(t,"clock_style"),0),0,3);
    }
    public static int clockSize(Context c,int t){
        return ThemeMath.clamp(ThemePrefs.prefs(c).getInt(k(t,"clock_size"),100),65,155);
    }
    public static int clockColor(Context c,int t){
        return ThemePrefs.prefs(c).getInt(k(t,"clock_color"),0);
    }
    public static int clockDx(Context c,int t){
        return ThemeMath.clamp(ThemePrefs.prefs(c).getInt(k(t,"clock_dx"),0),-65,65);
    }
    public static int clockDy(Context c,int t){
        return ThemeMath.clamp(ThemePrefs.prefs(c).getInt(k(t,"clock_dy"),0),-65,65);
    }
    public static int sceneDx(Context c,int t){
        return ThemeMath.clamp(ThemePrefs.prefs(c).getInt(k(t,"scene_dx"),0),-24,24);
    }
    public static int mode(Context c){
        return ThemeMath.clamp(ThemePrefs.prefs(c).getInt("studio_motion_mode",NORMAL),0,3);
    }
    public static void mode(Context c,int v){
        ThemePrefs.prefs(c).edit().putInt("studio_motion_mode",ThemeMath.clamp(v,0,3)).apply();
    }
    public static int fps(Context c,boolean powerSave){
        if(powerSave)return 12;
        int m=mode(c);
        if(m==ECO)return 12;
        if(ThemePrefs.batterySaver(c))return 15;
        if(m==CALM)return 20;
        return m==INTENSE?40:30;
    }
    public static float animationFactor(Context c){
        switch(mode(c)){
            case CALM:return .60f;
            case INTENSE:return 1.40f;
            case ECO:return .38f;
            default:return 1f;
        }
    }
    public static boolean transition(Context c){
        return ThemePrefs.prefs(c).getBoolean("studio_transition",true);
    }
    public static void transition(Context c,boolean enabled){
        ThemePrefs.prefs(c).edit().putBoolean("studio_transition",enabled).apply();
    }
    public static void put(Context c,int theme,String item,int value){
        android.content.SharedPreferences.Editor e=ThemePrefs.prefs(c).edit();
        if(item.equals("clock_size"))value=ThemeMath.clamp(value,65,155);
        if(item.equals("clock_dx")||item.equals("clock_dy"))
            value=ThemeMath.clamp(value,-65,65);
        if(item.equals("scene_dx"))value=ThemeMath.clamp(value,-24,24);
        if(item.equals("clock_style"))value=ThemeMath.clamp(value,0,3);
        e.putInt(k(theme,item),value).apply();
    }
    public static void reset(Context c,int theme){
        android.content.SharedPreferences.Editor edit=ThemePrefs.prefs(c).edit();
        for(String name:new String[]{"clock_style","clock_size","clock_color",
                                      "clock_dx","clock_dy","scene_dx"})
            edit.remove(k(theme,name));
        edit.apply();
    }
    public static int resolvedClockColor(Context c,int t,int fallback){
        int color=clockColor(c,t);
        return color==0?fallback:Color.rgb(Color.red(color),Color.green(color),Color.blue(color));
    }
}
