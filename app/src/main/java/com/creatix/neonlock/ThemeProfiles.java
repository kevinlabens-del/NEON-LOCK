package com.creatix.neonlock;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Private, named local theme variants. Import/export contains only visual settings,
 * no user identity, accounts, credentials or APK signing information.
 */
public final class ThemeProfiles {
    private static final String KEY="saved_theme_variants_v1";
    private static final int LIMIT=30;
    private static final String[] INTEGER_FIELDS={
        "accent","night_color","glow","size","speed","vertical",
        "lock_vertical","material","digits","center"
    };
    private static final String[] BOOLEAN_FIELDS={
        "adaptive","reverse","night_palette"
    };
    private ThemeProfiles(){}

    public static final class Variant {
        public final String id,name;
        public final int theme;
        public final JSONObject data;
        Variant(String id,String name,int theme,JSONObject data){
            this.id=id;this.name=name;this.theme=theme;this.data=data;
        }
    }

    private static String safeName(String value){
        if(value==null)value="";
        value=value.trim().replaceAll("[\\p{Cntrl}]","");
        if(value.length()>32)value=value.substring(0,32);
        return value.length()==0?"Création personnelle":value;
    }
    private static JSONArray raw(Context c){
        try{
            return new JSONArray(ThemePrefs.prefs(c).getString(KEY,"[]"));
        }catch(Exception error){return new JSONArray();}
    }
    public static ArrayList<Variant> list(Context c){
        JSONArray arr=raw(c);
        ArrayList<Variant> result=new ArrayList<>();
        for(int i=0;i<arr.length();i++){
            JSONObject item=arr.optJSONObject(i);
            if(item==null)continue;
            int index=item.optInt("theme",-1);
            if(index<0||index>=ThemePrefs.COUNT)continue;
            JSONObject data=item.optJSONObject("data");
            if(data==null)continue;
            result.add(new Variant(item.optString("id",""),
                safeName(item.optString("name","")),index,data));
        }
        return result;
    }
    private static JSONObject snapshot(Context c,int theme){
        JSONObject data=new JSONObject();
        int index=ThemePrefs.safe(theme);
        try{
            data.put("accent",ThemePrefs.accent(c,index));
            data.put("night_color",ThemePrefs.nightColor(c,index));
            data.put("glow",ThemePrefs.glow(c,index));
            data.put("size",ThemePrefs.size(c,index));
            data.put("speed",ThemePrefs.speed(c,index));
            data.put("vertical",ThemePrefs.vertical(c,index));
            data.put("lock_vertical",ThemePrefs.verticalForScreen(c,index,true));
            data.put("material",ThemePrefs.material(c,index));
            data.put("digits",ThemePrefs.digitStyle(c,index));
            data.put("center",ThemePrefs.centerStyle(c,index));
            data.put("adaptive",ThemePrefs.adaptive(c,index));
            data.put("reverse",ThemePrefs.reverse(c,index));
            data.put("night_palette",ThemePrefs.nightPalette(c,index));
        }catch(Exception ignored){}
        return data;
    }
    private static void store(Context c,ArrayList<Variant> data){
        JSONArray arr=new JSONArray();
        for(Variant profile:data){
            if(arr.length()>=LIMIT)break;
            JSONObject item=new JSONObject();
            try{
                item.put("id",profile.id);
                item.put("name",safeName(profile.name));
                item.put("theme",ThemePrefs.safe(profile.theme));
                item.put("data",profile.data);
                arr.put(item);
            }catch(Exception ignored){}
        }
        ThemePrefs.prefs(c).edit().putString(KEY,arr.toString()).apply();
    }
    public static String save(Context c,String name,int index){
        index=ThemePrefs.safe(index);
        ArrayList<Variant> existing=list(c);
        if(existing.size()>=LIMIT)return null;
        String id=UUID.randomUUID().toString();
        existing.add(new Variant(id,safeName(name),index,snapshot(c,index)));
        store(c,existing);
        return id;
    }
    public static boolean apply(Context c,String profileId){
        for(Variant item:list(c)){
            if(item.id.equals(profileId)){
                ThemePrefs.select(c,item.theme);
                JSONObject data=item.data;
                for(String field:INTEGER_FIELDS){
                    if(!data.has(field))continue;
                    int value=data.optInt(field,0);
                    if(field.equals("accent")){
                        ThemePrefs.setAccent(c,item.theme,value);
                        continue;
                    }
                    if(field.equals("night_color"))
                        value=value|0xff000000;
                    if(field.equals("glow"))value=ThemeMath.clamp(value,50,255);
                    if(field.equals("size"))value=ThemeMath.clamp(value,65,120);
                    if(field.equals("speed"))value=ThemeMath.clamp(value,20,200);
                    if(field.equals("vertical")||field.equals("lock_vertical"))
                        value=ThemeMath.clamp(value,35,80);
                    if(field.equals("material")||field.equals("center"))
                        value=ThemeMath.clamp(value,0,3);
                    if(field.equals("digits"))value=ThemeMath.clamp(value,0,2);
                    ThemePrefs.setInt(c,item.theme,field,value);
                }
                for(String field:BOOLEAN_FIELDS){
                    if(data.has(field))
                        ThemePrefs.setBoolean(c,item.theme,field,
                            data.optBoolean(field,false));
                }
                return true;
            }
        }
        return false;
    }
    public static void rename(Context c,String id,String newName){
        ArrayList<Variant> data=list(c);
        ArrayList<Variant> changed=new ArrayList<>();
        for(Variant item:data){
            if(item.id.equals(id))
                changed.add(new Variant(item.id,safeName(newName),item.theme,item.data));
            else changed.add(item);
        }
        store(c,changed);
    }
    public static void remove(Context c,String id){
        ArrayList<Variant> result=new ArrayList<>();
        for(Variant item:list(c))if(!item.id.equals(id))result.add(item);
        store(c,result);
    }
    public static String exportAll(Context c){
        JSONObject export=new JSONObject();
        try{
            export.put("format","NEON_LOCK_VISUAL_PROFILES_V1");
            export.put("version",1);
            export.put("selected_theme",ThemePrefs.selected(c));
            export.put("variants",raw(c));
        }catch(Exception ignored){}
        try{return export.toString(2);}catch(Exception ignored){return export.toString();}
    }
    public static int importAll(Context c,String json) throws Exception{
        if(json==null||json.length()>200000)
            throw new Exception("Le fichier est trop volumineux");
        JSONObject root=new JSONObject(json);
        if(!"NEON_LOCK_VISUAL_PROFILES_V1".equals(root.optString("format")))
            throw new Exception("Format de sauvegarde inconnu");
        JSONArray incoming=root.optJSONArray("variants");
        if(incoming==null)throw new Exception("Aucune variante trouvée");
        ArrayList<Variant> entries=list(c);
        int imported=0;
        for(int i=0;i<incoming.length()&&entries.size()<LIMIT;i++){
            JSONObject source=incoming.optJSONObject(i);
            if(source==null)continue;
            int theme=source.optInt("theme",-1);
            if(theme<0||theme>=ThemePrefs.COUNT)continue;
            JSONObject data=source.optJSONObject("data");
            if(data==null)continue;
            JSONObject clean=new JSONObject();
            for(String field:INTEGER_FIELDS)
                if(data.has(field))clean.put(field,data.optInt(field));
            for(String field:BOOLEAN_FIELDS)
                if(data.has(field))clean.put(field,data.optBoolean(field));
            entries.add(new Variant(UUID.randomUUID().toString(),
                safeName(source.optString("name","Importé")),theme,clean));
            imported++;
        }
        if(imported>0)store(c,entries);
        return imported;
    }
}
