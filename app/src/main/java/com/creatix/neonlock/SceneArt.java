package com.creatix.neonlock;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

/**
 * Environmental staging for the 11 non-Vault wallpapers, plus five new
 * independently composed scenes. All graphics are offline, scalable and
 * use no photo of the user or background tracking. The original Vault
 * is intentionally never sent through this renderer.
 */
final class SceneArt {
    private SceneArt(){}
    private static float clamp(float v){return Math.max(0f,Math.min(1f,v));}
    private static int translucent(int rgb,float opacity){
        return Color.argb(Math.round(clamp(opacity)*255f),
            Color.red(rgb),Color.green(rgb),Color.blue(rgb));
    }
    private static int blend(int a,int b,float t){
        t=clamp(t);
        return Color.rgb((int)(Color.red(a)*(1f-t)+Color.red(b)*t),
            (int)(Color.green(a)*(1f-t)+Color.green(b)*t),
            (int)(Color.blue(a)*(1f-t)+Color.blue(b)*t));
    }
    private static void line(Canvas c,Paint p,float x0,float y0,float x1,float y1,
                             int color,float width){
        p.reset();p.setAntiAlias(true);p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);p.setColor(color);p.setStrokeWidth(width);
        c.drawLine(x0,y0,x1,y1,p);
        p.setStyle(Paint.Style.FILL);
    }
    private static void circle(Canvas c,Paint p,float x,float y,float r,int color){
        p.reset();p.setAntiAlias(true);p.setStyle(Paint.Style.FILL);p.setColor(color);
        c.drawCircle(x,y,r,p);
    }
    private static void glowingRing(Canvas c,Paint p,float x,float y,float r,
                                    int accent,float width,float strength){
        p.reset();p.setAntiAlias(true);p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(width*3f);p.setColor(translucent(accent,.16f*strength));
        c.drawCircle(x,y,r,p);
        p.setStrokeWidth(width);p.setColor(translucent(accent,.75f*strength));
        c.drawCircle(x,y,r,p);
        p.setStyle(Paint.Style.FILL);
    }
    private static void polygon(Canvas c,Paint p,float x,float y,float r,
                                int sides,float rotate,int fill,int edge,float lineWidth){
        Path path=new Path();
        for(int i=0;i<sides;i++){
            double a=Math.toRadians(rotate+360.0*i/sides);
            float xx=x+(float)Math.cos(a)*r;
            float yy=y+(float)Math.sin(a)*r;
            if(i==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);
        }
        path.close();
        p.reset();p.setAntiAlias(true);p.setColor(fill);
        p.setStyle(Paint.Style.FILL);c.drawPath(path,p);
        p.setColor(edge);p.setStrokeWidth(lineWidth);
        p.setStyle(Paint.Style.STROKE);c.drawPath(path,p);
        p.setStyle(Paint.Style.FILL);
    }
    private static void rect(Canvas c,Paint p,float x0,float y0,float x1,float y1,int fill,
                             int border,float stroke){
        RectF box=new RectF(x0,y0,x1,y1);
        p.reset();p.setAntiAlias(true);
        p.setColor(fill);p.setStyle(Paint.Style.FILL);
        c.drawRoundRect(box,stroke*3f,stroke*3f,p);
        if(stroke>0){
            p.setColor(border);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(stroke);c.drawRoundRect(box,stroke*3f,stroke*3f,p);
            p.setStyle(Paint.Style.FILL);
        }
    }
    private static void lettering(Canvas c,Paint p,String s,float x,float y,
                                  float size,int color){
        p.reset();p.setAntiAlias(true);p.setStyle(Paint.Style.FILL);
        p.setColor(color);p.setTextSize(size);p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
        c.drawText(s,x,y,p);
    }
    private static void arc(Canvas c,Paint p,float x,float y,float radius,float start,
                            float sweep,float stroke,int color){
        p.reset();p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(stroke);p.setColor(color);
        c.drawArc(new RectF(x-radius,y-radius,x+radius,y+radius),start,sweep,false,p);
        p.setStyle(Paint.Style.FILL);
    }
    private static void prism(Canvas c,Paint p,float x,float y,float radius,
                              float rotate,int color,float strength){
        Path path=new Path();
        path.moveTo(x,y-radius);
        path.lineTo(x-radius*.64f,y+radius*.35f);
        path.lineTo(x+radius*.64f,y+radius*.35f);
        path.close();
        c.save();c.rotate(rotate,x,y);
        p.reset();p.setAntiAlias(true);
        p.setShader(new LinearGradient(x-radius*.7f,y-radius,x+radius*.7f,y+radius,
            new int[]{translucent(Color.WHITE,.6f*strength),color,
                translucent(color,.25f)},null,Shader.TileMode.CLAMP));
        c.drawPath(path,p);p.setShader(null);
        p.setColor(translucent(Color.WHITE,.65f*strength));
        p.setStrokeWidth(radius*.045f);p.setStyle(Paint.Style.STROKE);
        c.drawPath(path,p);c.restore();p.setStyle(Paint.Style.FILL);
    }
    private static void particles(Canvas c,Paint p,int w,int h,int seed,
                                  int count,int accent,float tick,float density){
        for(int i=0;i<count;i++){
            float a=(float)(i*2.399963+seed*1.237);
            float px=(float)((Math.sin(a*3.12d)*.48d+.5d)*w);
            float py=(float)((Math.cos(a*2.02d+tick*(.055+(i%4)*.018))*.48d+.5d)*h);
            float r=Math.max(.65f,Math.min(w,h)*(.0016f+(i%5)*.0008f));
            circle(c,p,px,py,r,translucent(i%4==0?Color.WHITE:accent,
                (.14f+(i%5)*.065f)*density));
        }
    }

    /**
     * Full-width scenery under the existing six HD ring textures.
     * Unlike the original Vault, these themes have varied environmental props.
     */
    static void scenery(Canvas c,Paint p,int w,int h,float x,float y,float r,
                        int theme,int accent,float glow,float tick,float offset){
        float drift=(offset-.5f)*r*.24f;
        int subtle=translucent(accent,.24f*glow);
        if(theme==ThemePrefs.CYBER){
            // Free-floating rectangular HUD screens and an angular circuit wall.
            for(int side=-1;side<=1;side+=2){
                float sx=x+side*r*1.13f+drift;
                rect(c,p,sx-r*.27f,y-r*.68f,sx+r*.27f,y+r*.67f,
                    0xff081a2b,subtle,r*.008f);
                for(int j=0;j<10;j++){
                    float yy=y-r*.56f+j*r*.122f;
                    line(c,p,sx-r*.19f,yy,sx+r*.11f,yy,
                        translucent(accent,.24f+j*.025f),r*.004f);
                }
                rect(c,p,sx-r*.16f,y-r*.78f,sx+r*.16f,y-r*.73f,
                    subtle,subtle,r*.003f);
            }
            for(int row=0;row<15;row++){
                float yy=y-r*1.65f+row*r*.24f;
                line(c,p,0,yy,w,yy,translucent(accent,.045f),r*.003f);
            }
            particles(c,p,w,h,2,22,accent,tick,glow*.7f);
        }else if(theme==ThemePrefs.GOLD){
            // Art-deco architecture, upright columns and stepped inlays.
            for(int side=-1;side<=1;side+=2){
                float px=x+side*r*1.17f+drift;
                rect(c,p,px-r*.09f,y-r*1.42f,px+r*.09f,y+r*1.43f,
                    0xff211509,0xffb18039,r*.016f);
                line(c,p,px-r*.045f,y-r*1.39f,px-r*.045f,y+r*1.39f,
                    translucent(0xffffde92,.56f),r*.009f);
                for(int j=0;j<6;j++){
                    float yy=y-r*1.22f+j*r*.49f;
                    polygon(c,p,px,yy,r*.076f,4,45f,
                        0xff402719,0xffffb86d,r*.006f);
                }
            }
            for(int i=0;i<5;i++){
                float ww=r*(1.24f-i*.11f);
                float yy=y-r*(1.42f-i*.075f);
                line(c,p,x-ww,yy,x+ww,yy,translucent(accent,.40f),r*.008f);
                line(c,p,x-ww,y*2-yy,x+ww,y*2-yy,
                    translucent(accent,.32f),r*.008f);
            }
        }else if(theme==ThemePrefs.RED){
            // Massive reactor housing, visible cooling pipes and warning slats.
            for(int side=-1;side<=1;side+=2){
                float xx=x+side*r*1.17f+drift;
                rect(c,p,xx-r*.12f,y-r*1.18f,xx+r*.12f,y+r*1.18f,
                    0xff2e151a,0xff8d343c,r*.014f);
                for(int i=0;i<12;i++){
                    float yy=y-r*.98f+i*r*.177f;
                    line(c,p,xx-r*.077f,yy,xx+r*.077f,yy,
                        translucent(accent,.17f+.16f*(i%3==0?1:0)),r*.039f);
                }
            }
            for(int j=0;j<6;j++){
                float sx=w*.08f+j*w*.17f;
                line(c,p,sx,0,sx+r*.10f,h,
                    translucent(accent,.036f),r*.015f);
            }
            particles(c,p,w,h,4,26,accent,tick,.8f*glow);
        }else if(theme==ThemePrefs.STEAMPUNK){
            // Asymmetric copper pipework and twin external clockwork fans.
            for(int side=-1;side<=1;side+=2){
                float xx=x+side*r*1.15f+drift;
                line(c,p,xx,y-r*1.62f,xx,y+r*1.48f,0xff4d2d16,r*.075f);
                line(c,p,xx,y-r*1.62f,xx,y+r*1.48f,0xffbb8550,r*.020f);
                for(int i=0;i<5;i++){
                    float yy=y-r*1.16f+i*r*.56f;
                    arc(c,p,xx,yy,r*.13f,85,210,r*.034f,0xffd5a66c);
                }
                polygon(c,p,xx,y-r*1.04f,r*.14f,12,tick*13f*side,
                    0xff5b371f,0xffc58c53,r*.022f);
            }
            for(int i=0;i<8;i++){
                float xx=x+(i%2==0?-1:1)*r*1.21f;
                float yy=y+r*.92f-((tick*.10f+i*.165f)%1f)*r*1.55f;
                circle(c,p,xx+(float)Math.sin(tick+i)*r*.09f,yy,r*.060f,
                    translucent(0xffe1b890,.04f*glow));
            }
        }else if(theme==ThemePrefs.GALAXY){
            // Nebula field covers the entire wallpaper; metallic ring is only
            // the gateway, never the boundary of the composition.
            for(int j=0;j<7;j++){
                float gx=(float)((Math.sin(j*2.42)*.43+.5)*w)+drift*.7f;
                float gy=(float)((Math.cos(j*1.83)*.45+.5)*h);
                p.setShader(new RadialGradient(gx,gy,r*(.45f+j*.04f),
                    new int[]{translucent(j%2==0?accent:0xff3871ff,.22f*glow),
                    Color.TRANSPARENT},null,Shader.TileMode.CLAMP));
                c.drawCircle(gx,gy,r*(.45f+j*.04f),p);p.setShader(null);
            }
            particles(c,p,w,h,8,70,0xffcb82ff,tick,glow);
            for(int i=0;i<9;i++){
                float xx=x+(float)Math.cos(i*1.93)*r*1.40f+drift;
                float yy=y+(float)Math.sin(i*1.93)*r*1.43f;
                line(c,p,xx-r*.09f,yy-r*.025f,xx+r*.07f,yy+r*.02f,
                    translucent(0xffb4a6f3,.20f),r*.007f);
            }
        }else if(theme==ThemePrefs.ARC){
            // Titanium shoulder plates create a reactor assembly instead of
            // a free-standing circle; blue conductor rails stay animated.
            for(int side=-1;side<=1;side+=2){
                float px=x+side*r*1.08f+drift*.5f;
                Path plate=new Path();
                plate.moveTo(px,y-r*1.45f);
                plate.lineTo(px+side*r*.32f,y-r*1.04f);
                plate.lineTo(px+side*r*.37f,y+r*.96f);
                plate.lineTo(px,y+r*1.45f);
                plate.lineTo(px-side*r*.13f,y);
                plate.close();
                p.setShader(new LinearGradient(px-r*.3f,y,px+r*.3f,y,
                    new int[]{0xff26384a,0xff8095a5,0xff111b2c},
                    null,Shader.TileMode.CLAMP));
                c.drawPath(plate,p);p.setShader(null);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(r*.018f);p.setColor(0xffa6baca);
                c.drawPath(plate,p);p.setStyle(Paint.Style.FILL);
                line(c,p,px,y-r*.88f,px,y+r*.88f,
                    translucent(accent,.68f*glow),r*.028f);
                for(int j=0;j<5;j++){
                    float yy=y-r*.78f+j*r*.36f;
                    polygon(c,p,px,yy,r*.074f,3,-90f,0xff052033,
                        translucent(accent,.65f),r*.009f);
                }
            }
            particles(c,p,w,h,10,23,accent,tick,.7f);
        }
    }

    /** Noncircular composition for each entirely new theme, 8 through 12. */
    static void newScene(Canvas c,Paint p,int w,int h,float x,float y,float r,
                         int id,int accent,float glow,float spin,float seconds,
                         float offset,int digitsStyle){
        float phase=seconds;
        if(id==ThemePrefs.NEON_GRID){
            neonGrid(c,p,w,h,x,y,r,accent,glow,spin,phase,offset);
        }else if(id==ThemePrefs.FROST){
            frost(c,p,w,h,x,y,r,accent,glow,spin,phase,offset);
        }else if(id==ThemePrefs.LAVA){
            lava(c,p,w,h,x,y,r,accent,glow,spin,phase,offset);
        }else if(id==ThemePrefs.SHADOW){
            shadow(c,p,w,h,x,y,r,accent,glow,spin,phase,offset);
        }else if(id==ThemePrefs.OCEAN){
            ocean(c,p,w,h,x,y,r,accent,glow,spin,phase,offset);
        }
        // Clock numerals move around their own themed geometry but never
        // rotate themselves, eliminating upside-down characters.
        uprightDigits(c,p,x,y,r,accent,spin,id,digitsStyle);
    }

    private static void uprightDigits(Canvas c,Paint p,float x,float y,float r,
                                      int accent,float spin,int theme,int style){
        p.reset();p.setAntiAlias(true);p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create(style==1?"serif":
            style==2?"monospace":"sans-serif-condensed",Typeface.BOLD));
        p.setTextSize(r*.137f);
        Paint.FontMetrics f=p.getFontMetrics();
        for(int i=0;i<7;i++){
            double angle=Math.toRadians(-90+i*360.0/7.0+spin*.23f);
            float xx=(float)Math.cos(angle),yy=(float)Math.sin(angle);
            float radial=theme==ThemePrefs.NEON_GRID?.76f:
                theme==ThemePrefs.SHADOW?.78f:.71f;
            float px=x+xx*r*radial;
            float py=y+yy*r*(theme==ThemePrefs.OCEAN?.61f:radial);
            p.setStyle(Paint.Style.STROKE);p.setColor(0xff040810);
            p.setStrokeWidth(r*.017f);
            c.drawText(Integer.toString(i),px,
                py-(f.ascent+f.descent)*.5f,p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(blend(accent,Color.WHITE,.48f));
            p.setShadowLayer(r*.015f,0,0,accent);
            c.drawText(Integer.toString(i),px,
                py-(f.ascent+f.descent)*.5f,p);
            p.clearShadowLayer();
        }
    }
    private static void neonGrid(Canvas c,Paint p,int w,int h,float x,float y,
                                 float r,int accent,float glow,float spin,float t,float offset){
        // Synthwave runway with real perspective grid, diagonal HUD wings and
        // a wide angular viewport: no circular bezel.
        int bg=0xff050917;
        p.setShader(new LinearGradient(0,0,0,h,
            new int[]{0xff020715,0xff11112d,0xff130524},null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);p.setShader(null);
        float horizon=y+r*.82f;
        for(int i=0;i<13;i++){
            float bx=(i-6)*w*.28f;
            line(c,p,w*.5f,horizon,w*.5f+bx,h,
                translucent(accent,.22f),r*.006f);
        }
        for(int j=1;j<=10;j++){
            float q=j/10f;
            float yy=horizon+(h-horizon)*q*q;
            line(c,p,0,yy,w,yy,translucent(accent,.17f+.1f*q),r*.004f);
        }
        rect(c,p,x-r*.92f,y-r*.76f,x+r*.92f,y+r*.77f,
            0xff091527,translucent(accent,.66f),r*.014f);
        polygon(c,p,x,y,r*.93f,6,-30f,translucent(0xff031121,.20f),
            translucent(accent,.70f),r*.012f);
        line(c,p,x-r*.81f,y-r*.69f,x-r*.61f,y-r*.81f,accent,r*.018f);
        line(c,p,x+r*.81f,y-r*.69f,x+r*.61f,y-r*.81f,accent,r*.018f);
        float scan=(float)Math.sin(t*1.75)*r*.54f;
        line(c,p,x-r*.62f,y+scan,x+r*.62f,y+scan,
            translucent(accent,.40f*glow),r*.008f);
        lettering(c,p,"NEON  /  MATRIX",x,y-r*.48f,r*.103f,
            translucent(accent,.78f));
        for(int i=0;i<10;i++){
            float q=i/9f;
            rect(c,p,x-r*.53f+i*r*.115f,y+r*.49f,
                x-r*.48f+i*r*.115f,y+r*.49f-r*(.08f+.18f*(float)
                    Math.abs(Math.sin(t*.90f+i*.56f))),
                translucent(accent,.40f),translucent(accent,.22f),r*.002f);
        }
        particles(c,p,w,h,13,16,accent,t,.55f);
    }
    private static void frost(Canvas c,Paint p,int w,int h,float x,float y,float r,
                              int accent,float glow,float spin,float t,float offset){
        p.setShader(new LinearGradient(0,0,w,h,
            new int[]{0xff06162c,0xff092e51,0xff031323,0xff08101f},
            null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);p.setShader(null);
        particles(c,p,w,h,18,55,0xffd5faff,t,glow);
        // Faceted crystals rise from each edge, casting ice-blue reflections.
        for(int i=0;i<11;i++){
            float xx=w*(i+.35f)/11f;
            float len=r*(.20f+(i%6)*.13f);
            prism(c,p,xx,0,len,180f,
                translucent(i%3==0?accent:0xffb9eaff,.50f),.57f);
            prism(c,p,xx,h,len*.80f,0f,
                translucent(0xffb0e9ff,.33f),.34f);
        }
        polygon(c,p,x,y,r*.98f,6,-90f,
            0xff09233a,translucent(0xffb3f2ff,.70f),r*.033f);
        polygon(c,p,x,y,r*.88f,6,-90f,
            0xff092c49,translucent(accent,.95f*glow),r*.018f);
        for(int i=0;i<6;i++){
            float a=i*60f+spin*.1f;
            double rad=Math.toRadians(a);
            float xx=x+(float)Math.sin(rad)*r*.88f;
            float yy=y-(float)Math.cos(rad)*r*.88f;
            prism(c,p,xx,yy,r*.27f,a,
                translucent(0xffc7f5ff,.65f),.45f);
        }
        polygon(c,p,x,y,r*.48f,6,-90f,
            0xff061a2d,translucent(0xffe0ffff,.68f),r*.014f);
        arc(c,p,x,y,r*.38f,spin*.30f,105f,r*.013f,
            translucent(accent,.70f));
        lettering(c,p,"FROST  CORE",x,y-r*.48f,r*.086f,
            translucent(0xffd3faff,.62f));
    }
    private static void lava(Canvas c,Paint p,int w,int h,float x,float y,float r,
                             int accent,float glow,float spin,float t,float offset){
        p.setShader(new LinearGradient(0,0,w,h,
            new int[]{0xff200804,0xff3c1008,0xff0c0709,0xff2e0b08},
            null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);p.setShader(null);
        // Vertical split-lava rivers and jagged rock columns.
        for(int side=-1;side<=1;side+=2){
            float xx=x+side*r*1.18f;
            Path river=new Path();
            river.moveTo(xx-r*.11f,0);
            for(int j=0;j<=18;j++){
                float yy=h*j/18f;
                float wave=(float)Math.sin(j*.75+t*.45f+side)*r*.09f;
                river.lineTo(xx+wave-r*.08f,yy);
            }
            for(int j=18;j>=0;j--){
                float yy=h*j/18f;
                float wave=(float)Math.sin(j*.75+t*.45f+side)*r*.09f;
                river.lineTo(xx+wave+r*.08f,yy);
            }
            river.close();
            p.setColor(translucent(accent,.62f));c.drawPath(river,p);
            p.setStyle(Paint.Style.STROKE);p.setColor(0xffffbc59);
            p.setStrokeWidth(r*.01f);c.drawPath(river,p);
            p.setStyle(Paint.Style.FILL);
        }
        polygon(c,p,x,y,r*.97f,8,22.5f,
            0xff24100d,0xff6f3220,r*.033f);
        polygon(c,p,x,y,r*.87f,8,22.5f,
            0xff120c0c,translucent(accent,.86f),r*.024f);
        for(int i=0;i<10;i++){
            float yy=y-r*.84f+i*r*.19f;
            float sx=(float)Math.sin(i*2.21+t*.08f)*r*.65f;
            float ex=(float)Math.cos(i*1.76+t*.09f)*r*.66f;
            line(c,p,x+sx,yy,x+ex,yy+r*.17f,
                translucent(i%3==0?0xffffb75c:accent,.45f*glow),r*.013f);
        }
        polygon(c,p,x,y,r*.44f,6,30f,
            0xff0b0e12,translucent(0xfff7ae66,.64f),r*.016f);
        for(int i=0;i<36;i++){
            float xx=(float)((Math.sin(i*21.9)*.45+.5)*w);
            float yy=(float)((Math.cos(i*4.12+t*(.12+i%4*.035))*.44+.5)*h);
            circle(c,p,xx,yy,r*(i%4==0?.010f:.005f),
                translucent(accent,.23f+(i%4)*.09f));
        }
        lettering(c,p,"MAGMA  CORE",x,y-r*.49f,r*.084f,
            translucent(accent,.66f));
    }
    private static void shadow(Canvas c,Paint p,int w,int h,float x,float y,float r,
                               int accent,float glow,float spin,float t,float offset){
        p.setShader(new LinearGradient(0,0,w,h,
            new int[]{0xff070d11,0xff0b181e,0xff030506,0xff102027},
            null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);p.setShader(null);
        // Dark tactical frame: asymmetrical fins, active radar, angled display.
        for(int i=0;i<10;i++){
            float yy=h*i/9f;
            line(c,p,0,yy,w,yy,translucent(0xff8bc2be,.045f),r*.002f);
        }
        for(int side=-1;side<=1;side+=2){
            float xx=x+side*r*1.05f;
            rect(c,p,xx-r*.105f,y-r*.99f,xx+r*.105f,y+r*.99f,
                0xff0e242a,translucent(0xff48665f,.45f),r*.012f);
            for(int i=0;i<14;i++){
                float yy=y-r*.84f+i*r*.13f;
                line(c,p,xx-r*.07f,yy,xx+r*.07f,yy,
                    translucent(accent,i%3==0?.43f:.1f),r*.012f);
            }
        }
        rect(c,p,x-r*.94f,y-r*.81f,x+r*.94f,y+r*.81f,
            0xff081218,translucent(accent,.57f),r*.015f);
        polygon(c,p,x,y,r*.91f,8,22.5f,
            0xff07141c,translucent(accent,.52f),r*.011f);
        // Sparse radar segment instead of another luminous full ring.
        arc(c,p,x,y,r*.73f,spin*.24f,50f,r*.017f,
            translucent(accent,.76f*glow));
        for(int i=0;i<7;i++){
            float xx=x+r*(-.53f+i*.18f);
            rect(c,p,xx,y+r*.54f,xx+r*.065f,y+r*.60f,
                translucent(accent,i%2==0?.30f:.52f),
                translucent(accent,.45f),r*.003f);
        }
        rect(c,p,x-r*.46f,y-r*.26f,x+r*.46f,y+r*.33f,
            0xff031018,translucent(accent,.30f),r*.006f);
        lettering(c,p,"SHADOW  //  SYSTEM",x,y-r*.48f,r*.083f,
            translucent(accent,.6f));
    }
    private static void ocean(Canvas c,Paint p,int w,int h,float x,float y,float r,
                              int accent,float glow,float spin,float t,float offset){
        p.setShader(new LinearGradient(0,0,0,h,
            new int[]{0xff031522,0xff063b49,0xff021426,0xff010910},
            null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p);p.setShader(null);
        // Sunbeams pass through water and open onto a wide porthole.
        for(int i=0;i<8;i++){
            float xx=w*(i+.35f)/8f;
            Path beam=new Path();beam.moveTo(xx-r*.04f,0);
            beam.lineTo(xx+r*.07f,0);
            beam.lineTo(xx+r*.55f,h);
            beam.lineTo(xx-r*.32f,h);beam.close();
            p.setColor(translucent(accent,.022f+(i%2)*.018f));
            c.drawPath(beam,p);
        }
        for(int i=0;i<44;i++){
            float xx=(float)((Math.sin(i*14.75)*.49+.5)*w);
            float yy=(float)((Math.cos(i*2.21+t*.27*(.35+i%4*.16))*.48+.5)*h);
            float rr=r*(.010f+i%4*.006f);
            p.setStyle(Paint.Style.STROKE);p.setColor(
                translucent(0xffb0efff,.19f+.09f*(i%3)));
            p.setStrokeWidth(r*.004f);c.drawCircle(xx,yy,rr,p);
            p.setStyle(Paint.Style.FILL);
        }
        // A wide elliptical submarine window surrounded by heavy brackets.
        p.setShader(new RadialGradient(x,y,r*1.15f,
            new int[]{0xff0a5b6b,0xff062a3c,0xff021020},
            null,Shader.TileMode.CLAMP));
        c.drawOval(new RectF(x-r*.98f,y-r*.79f,
            x+r*.98f,y+r*.79f),p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(r*.055f);p.setColor(0xff7f9d9d);
        c.drawOval(new RectF(x-r*.96f,y-r*.78f,x+r*.96f,y+r*.78f),p);
        p.setStrokeWidth(r*.018f);p.setColor(translucent(accent,.65f*glow));
        c.drawOval(new RectF(x-r*.89f,y-r*.72f,x+r*.89f,y+r*.72f),p);
        p.setStyle(Paint.Style.FILL);
        for(int i=0;i<12;i++){
            double a=i*Math.PI/6;
            circle(c,p,x+(float)Math.cos(a)*r*.94f,
                y+(float)Math.sin(a)*r*.76f,r*.020f,0xffc0d8d7);
        }
        // Slowly undulating illuminated water sheet below center.
        for(int j=0;j<5;j++){
            Path wave=new Path();
            for(int i=0;i<=36;i++){
                float xx=x-r*.75f+(i/36f)*r*1.5f;
                float yy=y+r*(.36f+j*.055f)+(float)Math.sin(
                    i*.40f+t*(.22f+j*.08f))*r*.014f;
                if(i==0)wave.moveTo(xx,yy);else wave.lineTo(xx,yy);
            }
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(r*.004f);
            p.setColor(translucent(accent,.20f+(.07f*j)));
            c.drawPath(wave,p);p.setStyle(Paint.Style.FILL);
        }
        lettering(c,p,"OCEAN  //  DEPTH",x,y-r*.49f,r*.087f,
            translucent(0xffcbfaff,.67f));
    }
}
