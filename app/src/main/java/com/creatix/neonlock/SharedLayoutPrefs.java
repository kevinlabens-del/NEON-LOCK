package com.creatix.neonlock;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Single layout profile for all 12 themes during automatic rotation.
 * The individual profiles are left untouched: switching back to individual
 * layout restores their previous settings. Colors and themed imagery are
 * never synchronized here.
 */
public final class SharedLayoutPrefs {
    private static final String ENABLED="shared_layout_enabled";
    private static final String INITIALIZED="shared_layout_initialized";
    private static final String PREFIX="shared_layout_";
    private static final String[] FIELDS={
        "vertical","lock_vertical","size","speed","glow","reverse",
        "scene_dx","clock_dx","clock_dy","clock_style",
        "clock_size","clock_color"
    };
    private SharedLayoutPrefs(){}

    static boolean enabled(Context c){
        SharedPreferences prefs=ThemePrefs.prefs(c);
        // Automatic cycling previously showed per-theme offsets. Enable
        // the common layout by default for existing cycling installations.
        return prefs.getBoolean(ENABLED,ThemePrefs.autoChange(c));
    }
    private static String perTheme(int theme,String field){
        return field.startsWith("clock_")||field.equals("scene_dx")?
            "studio_"+ThemePrefs.safe(theme)+"_"+field:
            "t"+ThemePrefs.safe(theme)+"_"+field;
    }
    private static int defaultValue(Context c,int theme,String field){
        switch(field){
            case "vertical":
                return theme==ThemePrefs.VAULT?
                    LockPrefs.get(c).verticalPercent:69;
            case "lock_vertical":
                return defaultValue(c,theme,"vertical");
            case "size":return 100;
            case "speed":return 100;
            case "glow":return 190;
            case "reverse":return 0;
            case "clock_size":return 100;
            default:return 0;
        }
    }
    /** Copy exactly the chosen theme's geometry into one authoritative profile. */
    public static synchronized void copyFromTheme(Context c,int theme){
        SharedPreferences p=ThemePrefs.prefs(c);
        SharedPreferences.Editor out=p.edit();
        theme=ThemePrefs.safe(theme);
        for(String field:FIELDS){
            int value;
            if(field.equals("reverse")){
                value=p.getBoolean(perTheme(theme,field),false)?1:0;
            }else{
                String key=perTheme(theme,field);
                if(field.equals("lock_vertical")&&!p.contains(key)){
                    value=p.getInt(perTheme(theme,"vertical"),
                        defaultValue(c,theme,"vertical"));
                }else{
                    value=p.getInt(key,defaultValue(c,theme,field));
                }
            }
            out.putInt(PREFIX+field,value);
        }
        out.putBoolean(INITIALIZED,true).apply();
    }
    private static void ensure(Context c){
        if(!enabled(c))return;
        SharedPreferences p=ThemePrefs.prefs(c);
        if(p.getBoolean(INITIALIZED,false))return;
        synchronized(SharedLayoutPrefs.class){
            if(!p.getBoolean(INITIALIZED,false))
                copyFromTheme(c,ThemePrefs.active(c));
        }
    }
    public static void setEnabled(Context c,boolean active){
        if(active)copyFromTheme(c,ThemePrefs.active(c));
        ThemePrefs.prefs(c).edit().putBoolean(ENABLED,active).apply();
    }
    static int read(Context c,int theme,String field,int fallback){
        if(!enabled(c))return fallback;
        ensure(c);
        return ThemePrefs.prefs(c).getInt(PREFIX+field,fallback);
    }
    static boolean readBoolean(Context c,int theme,String field,boolean fallback){
        return read(c,theme,field,fallback?1:0)!=0;
    }
    static boolean shares(String field){
        for(String name:FIELDS)if(name.equals(field))return true;
        return false;
    }
    static void onIntChanged(Context c,String field,int value){
        if(enabled(c)&&shares(field)){
            ensure(c);
            ThemePrefs.prefs(c).edit().putInt(PREFIX+field,value).apply();
        }
    }
    static void onBooleanChanged(Context c,String field,boolean value){
        onIntChanged(c,field,value?1:0);
    }
    /** Moves only the shared profile; no theme color, material or artwork changes. */
    public static void centerForLockScreen(Context c){
        if(!enabled(c))setEnabled(c,true);
        ThemePrefs.prefs(c).edit()
            .putInt(PREFIX+"vertical",62)
            .putInt(PREFIX+"lock_vertical",62)
            .putInt(PREFIX+"scene_dx",0)
            .putInt(PREFIX+"clock_dx",0)
            .putInt(PREFIX+"clock_dy",0).apply();
    }
}
