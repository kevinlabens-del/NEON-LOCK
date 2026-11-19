package com.creatix.neonlock;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Optional visual dial on the lock screen; never acts as a security boundary.
 *
 * The underlying Android Keyguard stays enabled. After correct visual code
 * or manual "Android" button, this service removes its overlay and the user
 * can unlock with the Android PIN, biometrics, or emergency controls.
 *
 * The service deliberately does not request accessibility window contents.
 * It observes only the device's screen/lock state via PowerManager/KeyguardManager.
 */
public final class NeonLockAccessibilityService extends AccessibilityService {
    private static final String TAG="NeonLockWake";
    private static final long TICK_MS=550L;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private WindowManager manager;
    private PowerManager power;
    private KeyguardManager keyguard;
    private FrameLayout root;
    private MainActivity.Wheel wheel;
    private final StringBuilder input=new StringBuilder();
    private boolean active=false,interacted=false,hiddenForWake=false,busy=false;
    private int failures=0;
    private final Runnable tick=new Runnable(){
        @Override public void run(){
            if(!active)return;
            checkState();
            handler.postDelayed(this,TICK_MS);
        }
    };

    @Override protected void onServiceConnected(){
        super.onServiceConnected();
        manager=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        power=(PowerManager)getSystemService(Context.POWER_SERVICE);
        keyguard=(KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        active=true;
        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        // A screen state transition can occur between timed checks.
        if(active && event!=null && event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            handler.post(this::checkState);
        }
    }

    @Override public void onInterrupt(){
        hideOverlay();
    }

    private void checkState(){
        if(!active)return;
        final boolean awake=power!=null && power.isInteractive();
        if(!awake){
            interacted=false;
            hiddenForWake=false;
            hideOverlay();
            return;
        }
        if(!interacted)interacted=true;
        boolean enabled=LockPrefs.prefs(this).getBoolean(LockPrefs.AUTO_DIAL,false);
        boolean locked=keyguard!=null && keyguard.isKeyguardLocked();
        int n=MainActivity.Credentials.length(this);
        if(!enabled || !locked || n<4){
            if(!locked)hiddenForWake=false;
            hideOverlay();
            return;
        }
        if(hiddenForWake){hideOverlay();return;}
        if(root==null)showOverlay(n);
    }

    private void showOverlay(int digits){
        input.setLength(0);
        busy=false;
        failures=0;
        FrameLayout container=new FrameLayout(this);
        container.setBackgroundColor(MainActivity.BG);
        MainActivity.Wheel visual=new MainActivity.Wheel(this,true);
        visual.setDemo(false);
        visual.setLength(digits);
        visual.setListener(this::acceptDigit);
        container.addView(visual,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout actions=new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        actions.setGravity(Gravity.CENTER_HORIZONTAL);
        actions.setPadding(MainActivity.dp(this,18),0,MainActivity.dp(this,18),
            MainActivity.dp(this,20));

        TextView info=MainActivity.text(this,
            "CODE VISUEL NEON • Puis déverrouillage Android",13,MainActivity.MUTED,false);
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

        LinearLayout.LayoutParams first=new LinearLayout.LayoutParams(0,
            MainActivity.dp(this,52),1f);
        first.setMargins(MainActivity.dp(this,4),0,MainActivity.dp(this,4),0);
        buttons.addView(erase,first);
        LinearLayout.LayoutParams second=new LinearLayout.LayoutParams(0,
            MainActivity.dp(this,52),1.2f);
        second.setMargins(MainActivity.dp(this,4),0,MainActivity.dp(this,4),0);
        buttons.addView(android,second);
        MainActivity.add(actions,buttons,this,10);

        FrameLayout.LayoutParams actionsLayout=new FrameLayout.LayoutParams(
            -1,-2,Gravity.BOTTOM);
        container.addView(actions,actionsLayout);

        WindowManager.LayoutParams params=new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START;
        params.setTitle("NEON LOCK cadran visuel");
        try{
            manager.addView(container,params);
            root=container;
            wheel=visual;
        }catch(Exception error){
            Log.w(TAG,"Cannot display optional lock overlay",error);
            hiddenForWake=true; // No repeated overlay retries until next wake.
            try{manager.removeViewImmediate(container);}catch(Exception ignored){}
        }
    }

    private void acceptDigit(int number){
        if(busy || root==null || wheel==null)return;
        int length=MainActivity.Credentials.length(this);
        if(length<4){dismissForWake();return;}
        if(input.length()>=length)return;
        input.append(number);
        wheel.setEntered(input.length());
        if(input.length()<length)return;
        busy=true;
        boolean correct=MainActivity.Credentials.check(this,input.toString());
        // Discard the digits as soon as they are verified.
        input.setLength(0);
        wheel.setState(correct?1:2);
        if(correct){
            handler.postDelayed(this::dismissForWake,850);
        }else{
            failures++;
            if(failures>=5){
                Toast.makeText(this,"Essais NEON incorrects. Utilise Android pour déverrouiller.",Toast.LENGTH_SHORT).show();
                handler.postDelayed(this::dismissForWake,1300);
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

    private void hideOverlay(){
        input.setLength(0);
        busy=false;
        FrameLayout old=root;
        root=null;
        wheel=null;
        if(old!=null && manager!=null){
            try{manager.removeViewImmediate(old);}catch(Exception error){
                Log.w(TAG,"Unable to remove visual overlay",error);
            }
        }
    }

    @Override public boolean onUnbind(android.content.Intent intent){
        active=false;
        handler.removeCallbacksAndMessages(null);
        hideOverlay();
        return super.onUnbind(intent);
    }

    @Override public void onDestroy(){
        active=false;
        handler.removeCallbacksAndMessages(null);
        hideOverlay();
        super.onDestroy();
    }
}
