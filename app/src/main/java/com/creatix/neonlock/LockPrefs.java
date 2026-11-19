package com.creatix.neonlock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/** Decorative live-wallpaper preferences only; no PIN or lock service. */
public final class LockPrefs {
    public static final String PREFS="neon_lock_style";
    public static final String SHOW_TITLE="show_title";
    public static final String SHOW_DATE="show_date";
    public static final String TITLE_COLOR="title_color";
    public static final String DATE_COLOR="date_color";
    public static final String RING_COLOR="ring_color";
    public static final String NUMBER_COLOR="number_color";
    public static final String CLOCK_COLOR="clock_color";
    public static final String DIAL_VERTICAL="dial_vertical_percent";
    public static final String NEON_INTENSITY="neon_intensity";
    public static final String REVERSE_ROTATION="reverse_rotation";
    public static final String SHOW_COUNTDOWN="show_countdown";
    private LockPrefs(){}

    public static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS,Context.MODE_PRIVATE);
    }

    public static Style get(Context context) {
        SharedPreferences p=prefs(context);
        return new Style(
            p.getBoolean(SHOW_TITLE,false),
            p.getBoolean(SHOW_DATE,false),
            p.getInt(TITLE_COLOR,Color.rgb(43,255,179)),
            p.getInt(DATE_COLOR,Color.rgb(178,199,227)),
            p.getInt(RING_COLOR,Color.rgb(255,171,43)),
            p.getInt(NUMBER_COLOR,Color.rgb(167,183,205)),
            p.getInt(CLOCK_COLOR,Color.rgb(232,241,254)),
            Math.max(40,Math.min(75,p.getInt(DIAL_VERTICAL,69))),
            Math.max(40,Math.min(245,p.getInt(NEON_INTENSITY,150))),
            p.getBoolean(REVERSE_ROTATION,false),
            p.getBoolean(SHOW_COUNTDOWN,true)
        );
    }

    public static final class Style {
        public final boolean showTitle,showDate;
        public final int titleColor,dateColor,ringColor,numberColor,clockColor,verticalPercent;
        public final int neonIntensity;
        public final boolean reverseRotation,showCountdown;
        public Style(boolean title,boolean date,int titleColor,int dateColor,
                     int ringColor,int numberColor,int clockColor,int verticalPercent,
                     int neonIntensity,boolean reverseRotation,boolean showCountdown) {
            this.showTitle=title;this.showDate=date;
            this.titleColor=titleColor;this.dateColor=dateColor;
            this.ringColor=ringColor;this.numberColor=numberColor;
            this.clockColor=clockColor;this.verticalPercent=verticalPercent;
        }
    }
}
