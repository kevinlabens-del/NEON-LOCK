package com.creatix.neonlock;

import org.junit.Test;
import static org.junit.Assert.*;

/** Regression checks for core live wallpaper math and scheduling. */
public final class ThemeMathTest {
    @Test public void sevenPositionsAreDistributedEqually(){
        assertEquals(-90f,ThemeMath.orbitAngle(0,0,1,false),.0001f);
        assertEquals(-90f+360f/7f,ThemeMath.orbitAngle(1,0,1,false),.0001f);
        assertEquals(-90f+6f*360f/7f,
            ThemeMath.orbitAngle(6,0,1,false),.0001f);
    }
    @Test public void oneMinuteCompletesOneFullRotation(){
        assertEquals(360f,
            ThemeMath.orbitAngle(0,60,1,false)-
            ThemeMath.orbitAngle(0,0,1,false),.001f);
        assertEquals(-360f,
            ThemeMath.orbitAngle(0,60,1,true)-
            ThemeMath.orbitAngle(0,0,1,true),.001f);
    }
    @Test public void parallaxIsBounded(){
        assertEquals(-12f,ThemeMath.parallax(0f,24f),.001f);
        assertEquals(0f,ThemeMath.parallax(.5f,24f),.001f);
        assertEquals(12f,ThemeMath.parallax(1f,24f),.001f);
        assertEquals(12f,ThemeMath.parallax(10f,24f),.001f);
    }
    @Test public void easingStaysWithinLimits(){
        assertEquals(0f,ThemeMath.easeOut(-1f),.001f);
        assertEquals(1f,ThemeMath.easeOut(1f),.001f);
        assertTrue(ThemeMath.easeOut(.5f)>.5f);
    }
    @Test public void nightScheduleCrossesMidnight(){
        assertTrue(ThemeMath.isNight(23));
        assertTrue(ThemeMath.isNight(0));
        assertTrue(ThemeMath.isNight(6));
        assertFalse(ThemeMath.isNight(7));
        assertFalse(ThemeMath.isNight(16));
    }
    @Test public void automaticThemeSelectionWraps(){
        assertEquals(0,ThemeMath.cyclingTheme(6,1,7));
        // Existing IDs are stable, but the modulo now spans twelve themes.
        assertEquals(0,ThemeMath.cyclingTheme(11,1,12));
        assertEquals(7,ThemeMath.cyclingTheme(11,8,12));
        assertEquals(9,ThemeMath.cyclingTheme(8,13,12));
        assertEquals(2,ThemeMath.cyclingTheme(6,3,7));
        assertEquals(4,ThemeMath.cyclingTheme(4,0,7));
    }
    @Test public void negativeAndNaNInputsCannotBreakGeometry(){
        assertEquals(35,ThemeMath.clamp(-1,35,80));
        assertEquals(80,ThemeMath.clamp(100,35,80));
        assertEquals(0f,ThemeMath.clamp(Float.NaN,0f,1f),.001f);
        assertEquals(0,ThemeMath.cyclingTheme(-1,0,7));
    }
    @Test public void textMustFitCenterFace(){
        assertTrue(ThemeMath.fitsCircle(90f,52f));
        assertFalse(ThemeMath.fitsCircle(101f,52f));
    }
}
