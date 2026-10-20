package nl.apk2tv;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.graphics.Path;
import android.content.SharedPreferences;
import android.view.WindowManager;
import android.util.DisplayMetrics;

public class RemoteTouchService extends AccessibilityService {
    private String foregroundPackage="";
    private long lastGesture=0;
    @Override public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e.getPackageName()!=null) foregroundPackage=e.getPackageName().toString();
    }
    @Override public void onInterrupt() {}
    @Override protected boolean onKeyEvent(KeyEvent e) {
        SharedPreferences prefs=getSharedPreferences("apk2tv",MODE_PRIVATE);
        if(!prefs.getBoolean("enabled",false))return false;
        String target=prefs.getString("target","").trim();
        if(target.isEmpty() || !target.equals(foregroundPackage))return false;
        int code=e.getKeyCode();
        if(code!=KeyEvent.KEYCODE_DPAD_UP && code!=KeyEvent.KEYCODE_DPAD_DOWN
            && code!=KeyEvent.KEYCODE_DPAD_LEFT && code!=KeyEvent.KEYCODE_DPAD_RIGHT
            && code!=KeyEvent.KEYCODE_DPAD_CENTER && code!=KeyEvent.KEYCODE_ENTER
            && code!=KeyEvent.KEYCODE_BUTTON_A)return false;
        if(e.getAction()==KeyEvent.ACTION_UP)return true;
        if(e.getRepeatCount()>0)return true;
        long now=android.os.SystemClock.uptimeMillis();
        if(now-lastGesture<110)return true;
        lastGesture=now;
        DisplayMetrics metrics=new DisplayMetrics();
        WindowManager wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(metrics);
        float x=metrics.widthPixels/2f,y=metrics.heightPixels/2f;
        float distance=Math.min(metrics.widthPixels,metrics.heightPixels)*0.18f;
        float endX=x,endY=y;
        if(code==KeyEvent.KEYCODE_DPAD_LEFT)endX-=distance;
        else if(code==KeyEvent.KEYCODE_DPAD_RIGHT)endX+=distance;
        else if(code==KeyEvent.KEYCODE_DPAD_UP)endY-=distance;
        else if(code==KeyEvent.KEYCODE_DPAD_DOWN)endY+=distance;
        Path path=new Path();path.moveTo(x,y);
        boolean tap=(code==KeyEvent.KEYCODE_DPAD_CENTER || code==KeyEvent.KEYCODE_ENTER || code==KeyEvent.KEYCODE_BUTTON_A);
        if(!tap)path.lineTo(endX,endY);
        GestureDescription.Builder builder=new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path,0,tap?55:190));
        dispatchGesture(builder.build(),null,null);
        return true;
    }
}
