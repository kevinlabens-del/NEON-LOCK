package com.creatix.neonlock;

import android.app.Activity;
import android.app.KeyguardManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

/**
 * Launches after a successful visual NEON code.
 * Requests legitimate Android Keyguard dismissal from a visible Activity.
 *
 * Without an Android-secure lock, the system can dismiss its swipe screen
 * immediately. With a secure lock, Android handles its own PIN/biometric UI.
 * It cannot be bypassed with the NEON visual code.
 */
public final class UnlockBridgeActivity extends Activity {
    private static final String TAG="NeonUnlockBridge";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private boolean requested=false;
    private boolean done=false;
    private final Runnable askAndroid=this::requestNativeDismiss;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        setShowWhenLocked(true);
        setTurnScreenOn(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(MainActivity.BG);
        getWindow().setNavigationBarColor(MainActivity.BG);

        FrameLayout root=new FrameLayout(this);
        root.setBackgroundColor(MainActivity.BG);
        LinearLayout contents=new LinearLayout(this);
        contents.setOrientation(LinearLayout.VERTICAL);
        contents.setGravity(Gravity.CENTER);
        contents.setPadding(MainActivity.dp(this,28),0,MainActivity.dp(this,28),0);
        TextView title=MainActivity.text(this,"✓  CODE NEON VALIDÉ",21,MainActivity.GREEN,true);
        contents.addView(title);
        TextView info=MainActivity.text(this,
            "Déverrouillage Android en cours…",15,MainActivity.WHITE,false);
        LinearLayout.LayoutParams infoParams=new LinearLayout.LayoutParams(-1,-2);
        infoParams.topMargin=MainActivity.dp(this,18);
        contents.addView(info,infoParams);
        TextView note=MainActivity.text(this,
            "Si Android demande un code PIN ou une empreinte, sa protection reste obligatoire.",
            13,MainActivity.MUTED,false);
        LinearLayout.LayoutParams noteParams=new LinearLayout.LayoutParams(-1,-2);
        noteParams.topMargin=MainActivity.dp(this,14);
        contents.addView(note,noteParams);
        FrameLayout.LayoutParams centerParams=new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER);
        root.addView(contents,centerParams);

        Button back=MainActivity.button(this,"REVENIR À ANDROID",MainActivity.PANEL,MainActivity.WHITE);
        back.setOnClickListener(v->end());
        FrameLayout.LayoutParams buttonParams=new FrameLayout.LayoutParams(-1,
            MainActivity.dp(this,58),Gravity.BOTTOM);
        buttonParams.setMargins(MainActivity.dp(this,24),0,
            MainActivity.dp(this,24),MainActivity.dp(this,28));
        root.addView(back,buttonParams);
        setContentView(root);
    }

    @Override protected void onResume(){
        super.onResume();
        if(!done && !requested){
            handler.removeCallbacks(askAndroid);
            handler.postDelayed(askAndroid,170);
        }
    }

    private void requestNativeDismiss(){
        if(done || requested)return;
        requested=true;
        final KeyguardManager manager=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        if(manager==null || !manager.isKeyguardLocked()){
            end();
            return;
        }
        try {
            manager.requestDismissKeyguard(this,new KeyguardManager.KeyguardDismissCallback(){
                @Override public void onDismissSucceeded(){
                    end();
                }
                @Override public void onDismissCancelled(){
                    end();
                }
                @Override public void onDismissError(){
                    Log.w(TAG,"Android rejected Keyguard dismissal");
                    end();
                }
            });
        }catch(Exception error){
            Log.w(TAG,"Cannot request Keyguard dismissal",error);
            end();
        }
    }

    private void end(){
        if(done)return;
        done=true;
        handler.removeCallbacksAndMessages(null);
        finish();
        overridePendingTransition(0,0);
    }

    @Override protected void onDestroy(){
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
