package com.creatix.neonlock;

/**
 * Device-independent studio calculations. Tested on CI via plain JUnit.
 */
public final class StudioMath {
    private StudioMath(){}
    public static int targetFps(int mode,boolean batterySaver,boolean powerSave){
        if(powerSave)return 12;
        if(mode==3)return 12; // economy
        if(batterySaver)return 15;
        if(mode==0)return 20; // calm
        if(mode==2)return 40; // intense
        return 30;
    }
    public static float motionFactor(int mode){
        if(mode==0)return .60f;
        if(mode==2)return 1.40f;
        if(mode==3)return .38f;
        return 1f;
    }
    public static float fadeProgress(long now,long started,long duration){
        if(started<=0 || duration<=0)return 1f;
        float progress=(now-started)/(float)duration;
        return ThemeMath.clamp(progress,0f,1f);
    }
    public static int draggedPercent(int initial,float deltaPixels,
                                     float viewportPixels,int low,int high){
        if(viewportPixels<=0||Float.isNaN(deltaPixels))
            return ThemeMath.clamp(initial,low,high);
        return ThemeMath.clamp(
            initial+Math.round(deltaPixels/viewportPixels*100f),low,high);
    }
}
