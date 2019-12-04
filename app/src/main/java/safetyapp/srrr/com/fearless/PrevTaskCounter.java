package safetyapp.srrr.com.fearless;

import android.os.CountDownTimer;

public abstract class PrevTaskCounter extends CountDownTimer {
    public abstract void onBeforeCount();

    public PrevTaskCounter(long millisInFuture, long countDownInterval){
        super(millisInFuture,countDownInterval);
    }

    public void count(){
        onBeforeCount();
        start();
    }
}
