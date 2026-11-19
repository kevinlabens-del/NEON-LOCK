package com.creatix.neonlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import android.util.Base64;

/**
 * NEON LOCK : custom visual dial and live wallpaper.
 * This application never bypasses, disables or replaces Android's secure Keyguard.
 */
public class MainActivity extends Activity {
    static final int BG = Color.rgb(6,9,17);
    static final int PANEL = Color.rgb(19,25,39);
    static final int ORANGE = Color.rgb(255,171,43);
    static final int GREEN = Color.rgb(43,255,179);
    static final int WHITE = Color.rgb(232,241,254);
    static final int MUTED = Color.rgb(142,159,184);
    private TextView status;
    private Wheel preview;

    static int dp(Context c, float value) {
        return Math.round(value * c.getResources().getDisplayMetrics().density);
    }

    static TextView text(Context c, String s, int size, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(size);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER_HORIZONTAL);
        return t;
    }

    static Button button(Context c, String s, int color, int foreground) {
        Button b = new Button(c);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTextColor(foreground);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        b.setMinHeight(dp(c,54));
        return b;
    }

    static void add(LinearLayout layout, View view, Context c, int margin) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0,dp(c,margin),0,0);
        layout.addView(view,p);
    }

    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        ScrollView sc = new ScrollView(this);
        sc.setBackgroundColor(BG);
        sc.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(this,22),dp(this,35),dp(this,22),dp(this,24));
        sc.addView(content);
        TextView mark = text(this,"CR3@TIX  /  ANDROID",12,GREEN,true);
        mark.setLetterSpacing(.21f);
        add(content,mark,this,0);
        TextView title = text(this,"NEON  LOCK",34,WHITE,true);
        title.setLetterSpacing(.1f);
        add(content,title,this,12);
        add(content,text(this,"Cadran numérique lumineux et fond d'écran animé.",15,MUTED,false),this,7);
        preview = new Wheel(this,false);
        preview.setDemo(true);
        preview.setWallpaperPreview(true);
        LinearLayout.LayoutParams frame = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,dp(this,325));
        frame.setMargins(0,dp(this,8),0,0);
        content.addView(preview,frame);

        Button settings = button(this,"⚙  PERSONNALISER LE VERROUILLAGE",GREEN,Color.BLACK);
        settings.setOnClickListener(v -> startActivity(new Intent(this,SettingsActivity.class)));
        add(content,settings,this,8);
        Button auto = button(this,"🔐  AFFICHER LE CADRAN AU RÉVEIL",ORANGE,Color.BLACK);
        auto.setOnClickListener(v -> startActivity(new Intent(this,SettingsActivity.class)));
        add(content,auto,this,8);
        Button test = button(this,"▶  TESTER LE CADRAN",ORANGE,Color.BLACK);
        test.setOnClickListener(v -> startActivity(new Intent(this,DialActivity.class)));
        add(content,test,this,8);
        Button wallpaper = button(this,"◉  FOND D'ÉCRAN ANIMÉ",PANEL,WHITE);
        wallpaper.setOnClickListener(v -> chooseWallpaper());
        add(content,wallpaper,this,8);
        Button pin = button(this,"✦  CHOISIR UN CODE VISUEL",PANEL,WHITE);
        pin.setOnClickListener(v -> selectPin());
        add(content,pin,this,8);
        status = text(this,"",13,GREEN,false);
        add(content,status,this,9);
        refreshStatus();

        Button clear = button(this,"EFFACER LE CODE VISUEL",BG,MUTED);
        clear.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle("Supprimer le code visuel ?")
            .setMessage("Le code sécurisé Android ne sera pas modifié.")
            .setNegativeButton("Annuler",null)
            .setPositiveButton("Supprimer",(d,w)->{ Credentials.clear(this); refreshStatus(); })
            .show());
        add(content,clear,this,4);
        add(content,text(this,
            "Important : Android garde toujours le contrôle de son déverrouillage " +
            "(code PIN, empreinte, visage). NEON LOCK ajoute un cadran visuel et un " +
            "fond d'écran et un mode cadran superposé facultatif. " +
            "Le vrai verrouillage Android reste nécessaire et peut être ouvert à tout moment.",13,MUTED,false),this,14);
        setContentView(sc);
    }

    @Override protected void onResume() {
        super.onResume();
        if(preview != null) preview.invalidate();
        if(status != null) refreshStatus();
    }

    private void refreshStatus() {
        int n=Credentials.length(this);
        String code=n>0?"Code visuel configuré : "+n+" chiffres.":
            "Mode démonstration : aucun code visuel configuré.";
        boolean automatic=LockPrefs.prefs(this).getBoolean(LockPrefs.AUTO_DIAL,false);
        status.setText(code+"\nCadran au réveil : "+(automatic?"activé (autorisation Android requise)":"désactivé"));
    }

    private void chooseWallpaper() {
        try {
            Intent in = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            in.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this,LiveWallpaper.class));
            startActivity(in);
        } catch (Exception ex) {
            try { startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)); }
            catch(Exception error) {
                Toast.makeText(this,"Le sélecteur de fonds animés est indisponible.",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void selectPin() {
        LinearLayout fields=new LinearLayout(this);
        fields.setOrientation(LinearLayout.VERTICAL);
        fields.setPadding(dp(this,20),0,dp(this,20),0);
        EditText a=new EditText(this), b=new EditText(this);
        a.setHint("Code visuel (4 à 6 chiffres)");
        b.setHint("Confirmer le code");
        int input=InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD;
        a.setInputType(input); b.setInputType(input);
        fields.addView(a); fields.addView(b);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Code visuel NEON")
            .setMessage("Choisis un code différent de ton PIN Android. Ce code n'est pas un verrouillage système.")
            .setView(fields).setNegativeButton("Annuler",null)
            .setPositiveButton("Enregistrer",null).create();
        dlg.setOnShowListener(v -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(w -> {
            String one=a.getText().toString(),two=b.getText().toString();
            if(!one.matches("[0-9]{4,6}")) { a.setError("4 à 6 chiffres"); return; }
            if(!one.equals(two)) { b.setError("Les codes ne correspondent pas"); return; }
            if(!Credentials.save(this,one)) {
                Toast.makeText(this,"Impossible d'enregistrer le code",Toast.LENGTH_SHORT).show();
                return;
            }
            refreshStatus();dlg.dismiss();
        }));
        dlg.show();
    }

    public static final class DialActivity extends Activity {
        private Wheel dial;
        private final StringBuilder input = new StringBuilder();
        private final Handler handler = new Handler(Looper.getMainLooper());
        private int digits,failures;
        private boolean demo,busy;

        @Override public void onCreate(Bundle state) {
            super.onCreate(state);
            setShowWhenLocked(true);
            getWindow().setStatusBarColor(BG);
            getWindow().setNavigationBarColor(BG);
            demo=Credentials.length(this)==0;
            digits=demo?4:Credentials.length(this);
            FrameLayout root = new FrameLayout(this);
            root.setBackgroundColor(BG);
            dial = new Wheel(this,true);
            dial.setLength(digits);
            dial.setDemo(demo);
            dial.setListener(n -> push(n));
            root.addView(dial,new FrameLayout.LayoutParams(-1,-1));
            LinearLayout actions = new LinearLayout(this);
            actions.setGravity(Gravity.CENTER);
            actions.setPadding(dp(this,18),0,dp(this,18),dp(this,14));
            Button back=button(this,"⌫  EFFACER",PANEL,WHITE);
            back.setOnClickListener(v -> {
                if(!busy&&input.length()>0) {input.deleteCharAt(input.length()-1);dial.setEntered(input.length());}
            });
            Button exit=button(this,"FERMER",PANEL,MUTED);
            exit.setOnClickListener(v -> finish());
            LinearLayout.LayoutParams item=new LinearLayout.LayoutParams(0,dp(this,55),1f);
            item.setMargins(dp(this,4),0,dp(this,4),0);
            actions.addView(back,item);
            LinearLayout.LayoutParams item2=new LinearLayout.LayoutParams(0,dp(this,55),1f);
            item2.setMargins(dp(this,4),0,dp(this,4),0);
            actions.addView(exit,item2);
            root.addView(actions,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));
            setContentView(root);
        }

        private void push(int n) {
            if(busy||input.length()>=digits)return;
            input.append(n);
            dial.setEntered(input.length());
            if(input.length()==digits) {
                busy=true;
                boolean valid=demo||Credentials.check(this,input.toString());
                dial.setState(valid?1:2);
                if(valid) {
                    handler.postDelayed(()->{
                        if(isFinishing()||isDestroyed())return;
                        if(demo)reset();
                        else unlockUsingAndroid();
                    },1200);
                } else {
                    failures++;
                    handler.postDelayed(()->{
                        if(isFinishing()||isDestroyed())return;
                        if(failures>=5){
                            Toast.makeText(this,"5 essais incorrects : attente de 30 secondes",Toast.LENGTH_LONG).show();
                            handler.postDelayed(()->{failures=0;reset();},30000);
                        } else reset();
                    },900);
                }
            }
        }

        private void reset(){
            input.setLength(0);busy=false;
            dial.setEntered(0);dial.setState(0);
        }
        private void unlockUsingAndroid(){
            KeyguardManager km=getSystemService(KeyguardManager.class);
            if(km==null||!km.isKeyguardLocked()) {finish();return;}
            km.requestDismissKeyguard(this,new KeyguardManager.KeyguardDismissCallback(){
                @Override public void onDismissSucceeded(){finish();}
                @Override public void onDismissCancelled(){reset();}
                @Override public void onDismissError(){
                    Toast.makeText(DialActivity.this,"Déverrouille avec Android",Toast.LENGTH_SHORT).show();
                    reset();
                }
            });
        }
        @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}
    }

    static final class Credentials {
        static final String PREF="neon_lock_visual";
        static int length(Context c){return c.getSharedPreferences(PREF,0).getInt("length",0);}
        static byte[] pbkdf(String value,byte[] salt)throws Exception{
            PBEKeySpec key = new PBEKeySpec(value.toCharArray(),salt,150000,256);
            try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(key).getEncoded();}
            finally{key.clearPassword();}
        }
        static boolean save(Context c,String value){
            try{
                byte[] salt=new byte[24];new SecureRandom().nextBytes(salt);
                String s=Base64.encodeToString(salt,Base64.NO_WRAP);
                String h=Base64.encodeToString(pbkdf(value,salt),Base64.NO_WRAP);
                return c.getSharedPreferences(PREF,0).edit().putString("salt",s)
                    .putString("hash",h).putInt("length",value.length()).commit();
            }catch(Exception e){return false;}
        }
        static boolean check(Context c,String value){
            try{
                SharedPreferences p=c.getSharedPreferences(PREF,0);
                byte[] salt=Base64.decode(p.getString("salt",""),Base64.NO_WRAP);
                byte[] stored=Base64.decode(p.getString("hash",""),Base64.NO_WRAP);
                return MessageDigest.isEqual(stored,pbkdf(value,salt));
            }catch(Exception e){return false;}
        }
        static void clear(Context c){c.getSharedPreferences(PREF,0).edit().clear().apply();}
    }

    interface OnDigit {void accept(int value);}

    public static final class Wheel extends View {
        private final boolean interactive;
        private float rotation,lastX,lastY,lastAngle;
        private int entered=0,length=4,state=0;
        private boolean demo=true,moved,wallpaperPreview,downOnRing;
        private OnDigit listener;
        private static final long SETTLE_DELAY_MS=450L;
        private final Handler settleHandler=new Handler(Looper.getMainLooper());
        private final Runnable confirmDigit=()->{
            if(listener!=null && state==0 && isAttachedToWindow()){
                performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                listener.accept(selected());
            }
        };

        Wheel(Context c,boolean interactive){
            super(c);
            this.interactive=interactive;
            if(interactive) setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            setContentDescription("Cadran numérique : tourne et relâche pour saisir automatiquement");
        }
        void setListener(OnDigit l){listener=l;}
        void setEntered(int v){entered=v;invalidate();}
        void setLength(int v){length=v;invalidate();}
        void setState(int v){
            state=v;
            if(v!=0) settleHandler.removeCallbacks(confirmDigit);
            invalidate();
        }
        void setDemo(boolean v){demo=v;invalidate();}
        void setWallpaperPreview(boolean v){wallpaperPreview=v;invalidate();}
        int selected(){return ((Math.round(-rotation/36f)%10)+10)%10;}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            Art.draw(canvas,getWidth(),getHeight(),rotation,selected(),entered,length,state,wallpaperPreview,demo,getContext());
        }
        private void scheduleConfirmation(){
            settleHandler.removeCallbacks(confirmDigit);
            if(listener!=null && state==0) {
                settleHandler.postDelayed(confirmDigit,SETTLE_DELAY_MS);
            }
        }
        @Override public boolean onTouchEvent(MotionEvent ev){
            if(!interactive || state!=0)return false;
            float x=ev.getX(), y=ev.getY(), cx=getWidth()*.5f,cy=getHeight()*.62f;
            float radius=Art.radius(getWidth(),getHeight(),false,.62f);
            float angle=(float)Math.toDegrees(Math.atan2(y-cy,x-cx));
            float distance=(float)Math.hypot(x-cx,y-cy);
            switch(ev.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    settleHandler.removeCallbacks(confirmDigit);
                    lastX=x;
                    lastY=y;
                    lastAngle=angle;
                    moved=false;
                    downOnRing=distance>=radius*.33f && distance<=radius*1.18f;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if(downOnRing && distance>radius*.30f){
                        float delta=angle-lastAngle;
                        if(delta>180f)delta-=360f;
                        if(delta< -180f)delta+=360f;
                        if(Math.abs(delta)<80f)rotation+=delta;
                    }
                    if(Math.hypot(x-lastX,y-lastY)>dp(getContext(),12))moved=true;
                    lastAngle=angle;
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    if(!moved && distance<radius*.33f){
                        // Maintient la confirmation manuelle comme solution de secours.
                        performClick();
                    }else if(downOnRing && moved){
                        rotation=Math.round(rotation/36f)*36f;
                        invalidate();
                        scheduleConfirmation();
                    }else if(!moved && downOnRing && distance<radius*1.18f){
                        int i=Math.round((angle+90f-rotation)/36f);
                        i=((i%10)+10)%10;
                        rotation=-i*36f;
                        invalidate();
                        scheduleConfirmation();
                    }
                    downOnRing=false;
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    downOnRing=false;
                    settleHandler.removeCallbacks(confirmDigit);
                    return true;
                default:return false;
            }
        }
        @Override public boolean performClick(){
            settleHandler.removeCallbacks(confirmDigit);
            super.performClick();
            if(listener!=null && state==0){
                performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                listener.accept(selected());
            }
            return true;
        }
        @Override protected void onDetachedFromWindow(){
            settleHandler.removeCallbacks(confirmDigit);
            super.onDetachedFromWindow();
        }
    }

    static final class Art {
        static void center(Canvas c,Paint p,String s,float x,float y,float size,int color,boolean bold){
            p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);
            p.setTextAlign(Paint.Align.CENTER);p.setTextSize(size);
            p.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));
            c.drawText(s,x,y,p);
        }
        static float radius(float w,float h,boolean wallpaper,float verticalFraction){
            float cy=h*verticalFraction;
            float base=Math.min(w*.39f,h*(wallpaper?.23f:.28f));
            float available=(h-cy-h*(wallpaper?.07f:.08f))/(wallpaper?1.15f:1.43f);
            return Math.max(1f,Math.min(base,available));
        }
        static void draw(Canvas c,float w,float h,float rotation,int selected,int entered,
                         int length,int state,boolean wallpaper,boolean demo,Context context){
            LockPrefs.Style style=LockPrefs.get(context);
            Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
            p.setShader(new LinearGradient(0,0,w,h,new int[]{Color.rgb(3,6,12),
                Color.rgb(14,20,34),Color.rgb(4,8,16)},null,Shader.TileMode.CLAMP));
            c.drawRect(0,0,w,h,p);p.setShader(null);
            float pos=wallpaper?style.verticalPercent/100f:.62f;
            float cx=w*.5f,cy=h*pos;
            float r=radius(w,h,wallpaper,pos);
            if(r<1f)return;
            int accent=state==1?GREEN:state==2?Color.rgb(255,77,104):style.ringColor;
            p.setShader(new RadialGradient(cx,cy,r*1.65f,
                new int[]{Color.argb(90,38,62,92),Color.argb(25,20,30,47),Color.TRANSPARENT},
                null,Shader.TileMode.CLAMP));
            c.drawCircle(cx,cy,r*1.65f,p);p.setShader(null);
            if(wallpaper){
                if(style.showTitle){
                    center(c,p,"NEON  LOCK",cx,h*.11f,r*.17f,style.titleColor,true);
                }
                if(style.showDate){
                    String date=new SimpleDateFormat("EEEE d MMMM",Locale.FRANCE).format(new Date());
                    center(c,p,date.toUpperCase(Locale.FRANCE),cx,h*.15f,r*.105f,style.dateColor,false);
                }
            }else{
                center(c,p,demo?"MODE DÉMONSTRATION":"CODE VISUEL LOCAL",cx,
                    Math.max(r*.3f,cy-r*1.52f),r*.115f,GREEN,true);
            }
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(13,19,34));c.drawCircle(cx,cy,r*1.08f,p);
            p.setShader(new RadialGradient(cx,cy,r,new int[]{
                Color.rgb(28,35,54),Color.rgb(13,18,29),Color.rgb(6,10,19)},
                new float[]{0,.62f,1f},Shader.TileMode.CLAMP));
            c.drawCircle(cx,cy,r*.95f,p);p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(r*.053f);
            p.setColor(Color.argb(60,Color.red(accent),Color.green(accent),Color.blue(accent)));
            c.drawArc(cx-r*1.07f,cy-r*1.07f,cx+r*1.07f,cy+r*1.07f,194,134,false,p);
            p.setColor(accent);p.setStrokeWidth(r*.022f);
            c.drawArc(cx-r*1.07f,cy-r*1.07f,cx+r*1.07f,cy+r*1.07f,194,134,false,p);
            c.drawArc(cx-r*1.12f,cy-r*1.12f,cx+r*1.12f,cy+r*1.12f,12,37,false,p);
            for(int tick=0;tick<120;tick++){
                float a=(float)Math.toRadians(tick*3-90+rotation);
                boolean major=tick%10==0;
                p.setColor(major?Color.rgb(123,145,165):Color.rgb(42,57,77));
                p.setStrokeWidth(r*(major?.013f:.005f));
                float inner=r*(major?.81f:.86f),outer=r*.92f;
                c.drawLine(cx+(float)Math.cos(a)*inner,cy+(float)Math.sin(a)*inner,
                    cx+(float)Math.cos(a)*outer,cy+(float)Math.sin(a)*outer,p);
            }
            p.setStyle(Paint.Style.FILL);
            for(int i=0;i<10;i++){
                float a=(float)Math.toRadians(i*36-90+rotation);
                float x=cx+(float)Math.cos(a)*r*.66f,y=cy+(float)Math.sin(a)*r*.66f;
                center(c,p,String.valueOf(i),x,y+r*.052f,r*(i==selected?.17f:.15f),
                    i==selected?accent:style.numberColor,i==selected);
            }
            Path arrow=new Path();
            arrow.moveTo(cx-r*.08f,cy-r*.99f);
            arrow.lineTo(cx+r*.08f,cy-r*.99f);
            arrow.lineTo(cx,cy-r*.87f);arrow.close();
            p.setColor(accent);c.drawPath(arrow,p);
            p.setColor(Color.rgb(7,11,20));c.drawCircle(cx,cy,r*.355f,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.014f);
            p.setColor(state==1?GREEN:Color.rgb(75,89,110));
            c.drawCircle(cx,cy,r*.355f,p);p.setStyle(Paint.Style.FILL);
            String middle=wallpaper?new SimpleDateFormat("HH:mm",Locale.FRANCE).format(new Date())
                :state==1?"✓":String.valueOf(selected);
            center(c,p,middle,cx,cy+r*.14f,r*(wallpaper?.29f:.45f),
                state==1?GREEN:style.clockColor,true);
            if(!wallpaper){
                for(int i=0;i<length;i++){
                    float x=cx+(i-(length-1)*.5f)*r*.19f;
                    p.setColor(i<entered?accent:Color.rgb(67,79,99));
                    c.drawCircle(x,cy-r*1.33f,r*.037f,p);
                }
                String hint=state==1?(demo?"DÉMO VALIDÉE":"ACCESS GRANTED")
                    :state==2?"CODE INCORRECT":"TOURNE • RELÂCHE • AUTO";
                center(c,p,hint,cx,cy+r*1.40f,r*.11f,
                    state==1?GREEN:state==2?Color.rgb(255,77,104):MUTED,true);
            }
        }
    }

    public static final class LiveWallpaper extends WallpaperService {
        @Override public Engine onCreateEngine(){return new NeonEngine();}
        private final class NeonEngine extends Engine {
            private final Handler handler=new Handler(Looper.getMainLooper());
            private final Runnable render=this::drawNow;
            private boolean visible;
            private float swipeStartX,swipeStartY;
            private boolean trackingSwipe;
            NeonEngine() {
                // Android peut transmettre les gestes aux fonds animés ; les OEM
                // ne sont pas obligés de les transmettre à l'écran verrouillé.
                setTouchEventsEnabled(true);
            }
            @Override public void onTouchEvent(MotionEvent event) {
                if(!visible || isPreview())return;
                KeyguardManager km=(KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
                if(km==null || !km.isKeyguardLocked())return;
                if(!LockPrefs.prefs(LiveWallpaper.this).getBoolean(LockPrefs.AUTO_DIAL,false) ||
                    !LockPrefs.prefs(LiveWallpaper.this).getBoolean(LockPrefs.SWIPE_FIRST,true))return;
                switch(event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        swipeStartX=event.getX();
                        swipeStartY=event.getY();
                        trackingSwipe=true;
                        break;
                    case MotionEvent.ACTION_UP:
                        if(trackingSwipe) {
                            float dx=event.getX()-swipeStartX;
                            float dy=event.getY()-swipeStartY;
                            if(dy< -dp(LiveWallpaper.this,62) && Math.abs(dy)>Math.abs(dx)*1.3f) {
                                NeonLockAccessibilityService.requestDialFromWallpaper();
                            }
                        }
                        trackingSwipe=false;
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        trackingSwipe=false;
                        break;
                }
            }
            @Override public void onVisibilityChanged(boolean v){
                visible=v;handler.removeCallbacks(render);
                if(v)drawNow();
            }
            @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int width,int height){
                super.onSurfaceChanged(holder,format,width,height);drawNow();
            }
            @Override public void onSurfaceDestroyed(SurfaceHolder holder){
                handler.removeCallbacks(render);super.onSurfaceDestroyed(holder);
            }
            @Override public void onDestroy(){
                visible=false;handler.removeCallbacks(render);super.onDestroy();
            }
            private void drawNow(){
                handler.removeCallbacks(render);
                Canvas c=null;
                try{
                    c=getSurfaceHolder().lockCanvas();
                    if(c!=null){
                        float a=(System.currentTimeMillis()%60000L)/1000f*6f;
                        int selected=((Math.round(-a/36f)%10)+10)%10;
                        Art.draw(c,c.getWidth(),c.getHeight(),a,selected,0,4,0,true,true,LiveWallpaper.this);
                    }
                }catch(Exception ignored){}
                finally{
                    if(c!=null)try{getSurfaceHolder().unlockCanvasAndPost(c);}
                    catch(Exception ignored){}
                }
                if(visible)handler.postDelayed(render,1000);
            }
        }
    }
}
