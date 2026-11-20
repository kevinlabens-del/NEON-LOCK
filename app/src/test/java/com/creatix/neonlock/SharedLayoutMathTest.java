package com.creatix.neonlock;

import org.junit.Test;
import static org.junit.Assert.*;

public final class SharedLayoutMathTest {
    @Test public void autoRotationSharesLayoutUnlessExplicitlyDisabled(){
        assertTrue(SharedLayoutMath.shouldShare(false,false,true));
        assertFalse(SharedLayoutMath.shouldShare(false,false,false));
        assertTrue(SharedLayoutMath.shouldShare(true,true,false));
        assertFalse(SharedLayoutMath.shouldShare(true,false,true));
    }
    @Test public void sameAlignmentAndSizeAcrossTwelveThemes(){
        for(int theme=0;theme<12;theme++){
            int individualPosition=35+theme*3;
            int individualScale=80+theme;
            assertEquals(62,SharedLayoutMath.effective(true,62,individualPosition));
            assertEquals(100,SharedLayoutMath.effective(true,100,individualScale));
            assertEquals(individualPosition,
                SharedLayoutMath.effective(false,62,individualPosition));
        }
    }
    @Test public void screenSafeBounds(){
        assertEquals(35,SharedLayoutMath.safePosition(18));
        assertEquals(80,SharedLayoutMath.safePosition(99));
        assertEquals(62,SharedLayoutMath.safePosition(62));
        assertEquals(65,SharedLayoutMath.safeScale(-2));
        assertEquals(120,SharedLayoutMath.safeScale(200));
    }
}
