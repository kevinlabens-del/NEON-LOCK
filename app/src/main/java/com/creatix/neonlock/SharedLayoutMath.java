package com.creatix.neonlock;

/** Pure policies tested on GitHub without an Android emulator. */
public final class SharedLayoutMath {
    private SharedLayoutMath(){}
    public static boolean shouldShare(boolean explicitSettingExists,
                                      boolean explicitValue,boolean autoChange){
        return explicitSettingExists?explicitValue:autoChange;
    }
    public static int effective(boolean commonLayout,int shared,int individual){
        return commonLayout?shared:individual;
    }
    public static int safePosition(int percentage){
        return ThemeMath.clamp(percentage,35,80);
    }
    public static int safeScale(int percentage){
        return ThemeMath.clamp(percentage,65,120);
    }
}
