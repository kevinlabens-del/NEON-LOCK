package com.creatix.neonlock;

/** Pure geometry and scheduling helpers; testable without an Android device. */
public final class ThemeMath {
    private ThemeMath(){}
    public static int clamp(int value,int min,int max){
        return Math.max(min,Math.min(max,value));
    }
    public static float clamp(float value,float min,float max){
        if(Float.isNaN(value))return min;
        return Math.max(min,Math.min(max,value));
    }
    public static float easeOut(float value){
        float t=clamp(value,0f,1f);
        return 1f-(1f-t)*(1f-t)*(1f-t);
    }
    public static float pulse(float seconds,float period){
        return (float)(.5+.5*Math.sin(seconds*2.0*Math.PI/Math.max(.01,period)));
    }
    /** Seven digits orbit the dial, but their glyph rotation is always zero degrees. */
    public static float orbitAngle(int digit,float seconds,float speed,boolean reverse){
        float delta=seconds*6f*clamp(speed,0f,2f)*(reverse?-1f:1f);
        return -90f+digit*(360f/7f)+delta;
    }
    public static float parallax(float screenOffset,float scale){
        return (clamp(screenOffset,0f,1f)-.5f)*scale;
    }
    public static boolean isNight(int localHour){
        return localHour>=22 || localHour<7;
    }
    public static int cyclingTheme(int anchor,long cycles,int count){
        int size=Math.max(1,count);
        long a=((long)anchor+Math.max(0,cycles))%size;
        return (int)((a+size)%size);
    }
    public static boolean fitsCircle(float textWidth,float centerRadius){
        return textWidth<=centerRadius*1.8f;
    }
}
