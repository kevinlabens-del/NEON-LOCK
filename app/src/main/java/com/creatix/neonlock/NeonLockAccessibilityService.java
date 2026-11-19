package com.creatix.neonlock;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.ref.WeakReference;

/**
 * Screen-on, optional visual dial for a regular Android device (no root).
 *
 * State machine:
 *   Locked + awake + swipe-first -> small gesture handle above the lock wallpaper;
 *   swipe on the wallpaper (if dispatched) or on handle -> full screen NEON dial;
 *   correct visual code -> remove window, hand control to Android Keyguard.
 *
 * Underlying Android lock is never disabled or bypassed. The overlay is not
 * an authentication boundary: "PASSER À ANDROID" always dismisses it.
 * Accessibility window contents are never requested.
 */
public final class NeonLockAccessibilityService extends AccessibilityService {
    private static final String TAG = "NeonLockWake";
    private static final long TICK_MS = 500L;
    private static WeakReference<NeonLockAccessibilityService> activeInstance =
        new WeakReference<>(null);

    // Can be called by the app's own WallpaperService (both use the app's default process).
    // A service killed or restricted by Android cannot be revived through this method.
    public static void requestDialFromWallpaper() {
        NeonLockAccessibilityService service=activeInstance.get();
        if(service!=null)service.handler.post(service::safelyShowDialOnUserGesture);
    }

    // Réglages : état de connexion réel, indépendant du simple interrupteur Android.
    private static volatile String lastIssue="";
    public static boolean isConnected(){
        NeonLockAccessibilityService service=activeInstance.get();
        return service!=null && service.active;
    }
    public static String getLastIssue(){ return lastIssue; }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager manager;
    private PowerManager power;
    private KeyguardManager keyguard;
    private View root;
    private MainActivity.Wheel wheel;
    private final StringBuilder input = new StringBuilder();
    private boolean active=false,hiddenForWake=false,busy=false;
    private int failures=0,mode=0; // 0 absent, 1 gesture handle, 2 full dial

    private final Runnable tick=new Runnable(){
        @Override public void run(){
            if(!active)return;
            safelyCheckState();
            handler.postDelayed(this,TICK_MS);
        }
    };

    @Override protected void onServiceConnected(){
        super.onServiceConnected();
        manager=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        power=(PowerManager)getSystemService(Context.POWER_SERVICE);
        keyguard=(KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        active=true;
        lastIssue="";
        activeInstance=new WeakReference<>(this);
        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        // Deliberately do not inspect content, text or nodes of other apps.
        if(active && event!=null &&
            event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            handler.post(this::safelyCheckState);
        }
    }

    @Override public void onInterrupt(){
        hideOverlay();
    }

    private void safelyCheckState(){
        try { checkState(); }
        catch(RuntimeException error){
            Log.e(TAG,"NEON service cannot update screen",error);
            lastIssue="Erreur d'affichage : vérifie le service Android";
            hideOverlay();
        }
    }

    private void safelyShowDialOnUserGesture(){
        try { showDialOnUserGesture(); }
        catch(RuntimeException error){
            Log.e(TAG,"NEON dial opening failed",error);
            lastIssue="Impossible d'ouvrir le cadran après le geste";
            hideOverlay();
        }
    }

    private boolean isAwakeAndLocked(){
        return active && power!=null && power.isInteractive() &&
            keyguard!=null && keyguard.isKeyguardLocked();
    }

    private boolean isNeonEnabled(){
        return LockPrefs.prefs(this).getBoolean(LockPrefs.AUTO_DIAL,false) &&
            MainActivity.Credentials.length(this)>=4;
    }

    private boolean swipeFirst(){
        return LockPrefs.prefs(this).getBoolean(LockPrefs.SWIPE_FIRST,true);
    }

    private void checkState(){
        if(!active)return;
        if(power==null || !power.isInteractive()){
            hiddenForWake=false;
            hideOverlay();
            return;
        }
        if(!isAwakeAndLocked()){
            hiddenForWake=false;
            hideOverlay();
            return;
        }
        if(!isNeonEnabled()){
            hideOverlay();
            return;
        }
        if(hiddenForWake){
            hideOverlay();
            return;
        }
        if(root==null){
            if(swipeFirst())showSwipeHint();
            else showDialOverlay();
        }
    }

    private WindowManager.LayoutParams windowParams(int width,int height,int gravity,int offsetY,String title){
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(
            width,height,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        p.gravity=gravity;
        p.y=offsetY;
        p.setTitle(title);
        return p;
    }

    private void showSwipeHint(){
        if(manager==null || root!=null)return;
        FrameLayout hint=new FrameLayout(this);
        int border=MainActivity.dp(this,1);
        GradientDrawable background=new GradientDrawable();
        background.setColor(Color.argb(225,10,16,31));
        background.setCornerRadius(MainActivity.dp(this,26));
        background.setStroke(border,Color.rgb(43,255,179));
        hint.setBackground(background);
        TextView label=MainActivity.text(this,"↑  BALAYER POUR LE CODE NEON",14,MainActivity.GREEN,true);
        label.setGravity(Gravity.CENTER);
        hint.addView(label,new FrameLayout.LayoutParams(-1,-1));

        final float[] origin={0,0};
        hint.setOnTouchListener((view,event)->{
            switch(event.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    origin[0]=event.getX();origin[1]=event.getY();
                    return true;
                case MotionEvent.ACTION_UP:
                    float dx=event.getX()-origin[0];
                    float dy=event.getY()-origin[1];
                    // Clicking the hint is a reliable fallback on OEMs that
                    // do not forward wallpaper touch events.
                    if(dy< -MainActivity.dp(this,35) && Math.abs(dy)>Math.abs(dx)*1.15f
                        || Math.hypot(dx,dy)<MainActivity.dp(this,15)){
                        showDialOnUserGesture();
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:return true;
                default:return true;
            }
        });
        int displayWidth=getResources().getDisplayMetrics().widthPixels;
        int width=Math.min(displayWidth-MainActivity.dp(this,52),MainActivity.dp(this,330));
        WindowManager.LayoutParams params=windowParams(width,MainActivity.dp(this,62),
            Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,MainActivity.dp(this,155),
            "NEON LOCK — glisser pour saisir le code");
        try{
            manager.addView(hint,params);
            root=hint;mode=1;lastIssue="";
        }catch(Exception failure){
            lastIssue="Android refuse le bouton de balayage";
            hiddenForWake=true;hideOverlay();
            Log.w(TAG,"Cannot show swipe affordance",failure);
        }
    }

    private void showDialOnUserGesture(){
        if(!isAwakeAndLocked() || !isNeonEnabled() || hiddenForWake)return;
        if(mode==2)return;
        hideOverlay();
        showDialOverlay();
    }

    private void showDialOverlay(){
        if(manager==null || root!=null)return;
        int length=MainActivity.Credentials.length(this);
        if(length<4)return;
        input.setLength(0);
        busy=false;
        failures=0;
        FrameLayout container=new FrameLayout(this);
        container.setBackgroundColor(MainActivity.BG);
        MainActivity.Wheel visual=new MainActivity.Wheel(this,true);
        visual.setDemo(false);
        visual.setLength(length);
        visual.setListener(this::acceptDigit);
        container.addView(visual,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout actions=new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        actions.setGravity(Gravity.CENTER);
        actions.setPadding(MainActivity.dp(this,18),0,MainActivity.dp(this,18),
            MainActivity.dp(this,20));
        TextView info=MainActivity.text(this,
            "CODE VISUEL NEON • PUIS DÉVERROUILLAGE ANDROID",12,MainActivity.MUTED,false);
        MainActivity.add(actions,info,this,0);

        LinearLayout buttons=new LinearLayout(this);
        buttons.setGravity(Gravity.CENTER);
        Button erase=MainActivity.button(this,"⌫  EFFACER",MainActivity.PANEL,MainActivity.WHITE);
        erase.setOnClickListener(v->{
            if(!busy && input.length()>0 && wheel!=null){
                input.deleteCharAt(input.length()-1);
                wheel.setEntered(input.length());
            }
        });
        Button android=MainActivity.button(this,"PASSER À ANDROID",MainActivity.GREEN,Color.BLACK);
        android.setOnClickListener(v->dismissForWake());
        LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,MainActivity.dp(this,52),1f);
        left.setMargins(MainActivity.dp(this,4),0,MainActivity.dp(this,4),0);
        buttons.addView(erase,left);
        LinearLayout.LayoutParams right=new LinearLayout.LayoutParams(0,MainActivity.dp(this,52),1.2f);
        right.setMargins(MainActivity.dp(this,4),0,MainActivity.dp(this,4),0);
        buttons.addView(android,right);
        MainActivity.add(actions,buttons,this,10);
        container.addView(actions,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));

        WindowManager.LayoutParams p=windowParams(-1,-1,Gravity.TOP|Gravity.START,0,
            "NEON LOCK — saisie du code visuel");
        try{
            manager.addView(container,p);
            root=container;wheel=visual;mode=2;lastIssue="";
        }catch(Exception failure){
            lastIssue="Android refuse d'afficher le cadran";
            Log.w(TAG,"Cannot show dial on top of Android lockscreen",failure);
            hiddenForWake=true;
            hideOverlay();
        }
    }

    private void acceptDigit(int digit){
        if(busy || mode!=2 || wheel==null)return;
        int digits=MainActivity.Credentials.length(this);
        if(digits<4){dismissForWake();return;}
        if(input.length()>=digits)return;
        input.append(digit);
        wheel.setEntered(input.length());
        if(input.length()<digits)return;
        busy=true;
        boolean valid=MainActivity.Credentials.check(this,input.toString());
        input.setLength(0);
        wheel.setState(valid?1:2);
        if(valid){
            handler.postDelayed(this::finishUnlockWithAndroid,850);
        }else{
            failures++;
            if(failures>=5){
                Toast.makeText(this,"5 essais : utiliser Android pour déverrouiller",
                    Toast.LENGTH_SHORT).show();
                handler.postDelayed(this::dismissForWake,1250);
            }else{
                handler.postDelayed(()->{
                    busy=false;
                    if(wheel!=null){wheel.setState(0);wheel.setEntered(0);}
                },900);
            }
        }
    }

    private void dismissForWake(){
        hiddenForWake=true;
        hideOverlay();
    }

    private void finishUnlockWithAndroid(){
        // Mode de base v0.5 confirmé sur téléphone : aucune activité de transition.
        // Le mode de demande Android reste facultatif et expérimental.
        boolean nativeRequest=LockPrefs.prefs(this)
            .getBoolean(LockPrefs.NATIVE_UNLOCK_AFTER_NEON,false);
        if(!nativeRequest){
            dismissForWake();
            return;
        }
        hiddenForWake=true;
        if(!isAwakeAndLocked()){
            hideOverlay();
            return;
        }
        try {
            Intent intent=new Intent(this,UnlockBridgeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        }catch(Exception failure){
            Log.w(TAG,"Cannot start Android unlock bridge",failure);
            lastIssue="Déverrouille avec Android ; transition automatique refusée";
        }finally {
            hideOverlay();
        }
    }

    private void hideOverlay(){
        input.setLength(0);busy=false;
        View previous=root;
        root=null;wheel=null;mode=0;
        if(previous!=null && manager!=null){
            try{manager.removeViewImmediate(previous);}
            catch(Exception failure){Log.w(TAG,"Cannot remove overlay",failure);}
        }
    }

    @Override public boolean onUnbind(android.content.Intent intent){
        active=false;
        activeInstance.clear();
        handler.removeCallbacksAndMessages(null);
        hideOverlay();
        return super.onUnbind(intent);
    }

    @Override public void onDestroy(){
        active=false;
        activeInstance.clear();
        handler.removeCallbacksAndMessages(null);
        hideOverlay();
        super.onDestroy();
    }
}
