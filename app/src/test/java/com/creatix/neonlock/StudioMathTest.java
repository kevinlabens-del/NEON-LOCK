package com.creatix.neonlock;

import org.junit.Test;
import static org.junit.Assert.*;

public final class StudioMathTest {
    @Test public void animationModesRespectBattery(){
        assertEquals(20,StudioMath.targetFps(0,false,false));
        assertEquals(30,StudioMath.targetFps(1,false,false));
        assertEquals(40,StudioMath.targetFps(2,false,false));
        assertEquals(12,StudioMath.targetFps(3,false,false));
        assertEquals(15,StudioMath.targetFps(2,true,false));
        assertEquals(12,StudioMath.targetFps(2,false,true));
        assertEquals(1.4f,StudioMath.motionFactor(2),.001f);
        assertEquals(.38f,StudioMath.motionFactor(3),.001f);
    }
    @Test public void crossfadeBounds(){
        assertEquals(0f,StudioMath.fadeProgress(1000,1000,750),.001f);
        assertEquals(.5f,StudioMath.fadeProgress(1375,1000,750),.001f);
        assertEquals(1f,StudioMath.fadeProgress(1750,1000,750),.001f);
        assertEquals(1f,StudioMath.fadeProgress(1900,1000,750),.001f);
        assertEquals(0f,StudioMath.fadeProgress(800,1000,750),.001f);
        assertEquals(1f,StudioMath.fadeProgress(1000,0,750),.001f);
    }
    @Test public void touchDraggingClamps(){
        assertEquals(75,StudioMath.draggedPercent(50,200,800,35,80));
        assertEquals(35,StudioMath.draggedPercent(50,-300,800,35,80));
        assertEquals(80,StudioMath.draggedPercent(50,1200,800,35,80));
        assertEquals(55,StudioMath.draggedPercent(55,0,800,35,80));
        assertEquals(55,StudioMath.draggedPercent(55,500,0,35,80));
    }
}
