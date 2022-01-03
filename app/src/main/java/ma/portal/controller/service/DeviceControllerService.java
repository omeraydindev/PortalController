package ma.portal.controller.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

import ma.portal.controller.Constants;
import ma.portal.controller.R;
import ma.portal.controller.model.BasicPath;
import ma.portal.controller.model.ControlPacket;
import ma.portal.controller.util.AndroidUtil;
import ma.portal.controller.util.MathUtil;

public class DeviceControllerService extends AccessibilityService {
    // yes yes, i know. but Android provides literally no way
    // to continuously communicate between an Activity and an AccessibilityService.
    public static DeviceControllerService instance;

    FrameLayout mLayout;

    @Override
    protected void onServiceConnected() {
        instance = this;

        mLayout = new FrameLayout(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM | Gravity.START;

        LayoutInflater.from(this)
                .inflate(R.layout.touch_service_handle, mLayout);
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(mLayout, lp);
    }

    @Override
    public void onDestroy() {
        instance = null;
    }

    public void process(ControlPacket controlPacket) {
        switch (controlPacket.getData().getAction()) {
            case ControlPacket.Data.ACTION_GESTURE: {
                BasicPath basicPath = controlPacket.getData().getGesture();
                dispatchGesture(basicPath);
                break;
            }
            case ControlPacket.Data.ACTION_BACK: {
                performGlobalAction(GLOBAL_ACTION_BACK);
                break;
            }
            case ControlPacket.Data.ACTION_HOME: {
                performGlobalAction(GLOBAL_ACTION_HOME);
                break;
            }
            case ControlPacket.Data.ACTION_RECENTS: {
                performGlobalAction(GLOBAL_ACTION_RECENTS);
                break;
            }
            case ControlPacket.Data.ACTION_VOLUP: {
                adjustVolume(AudioManager.ADJUST_RAISE);
                break;
            }
            case ControlPacket.Data.ACTION_VOLDOWN: {
                adjustVolume(AudioManager.ADJUST_LOWER);
                break;
            }
            case ControlPacket.Data.ACTION_LOCKSCR: {
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                if (powerManager.isInteractive()) { // device is awake
                    lockScreen();
                } else { // device is asleep
                    performGlobalAction(GLOBAL_ACTION_HOME);
                }
                break;
            }
        }
    }

    private void dispatchGesture(BasicPath basicPath) {
        dispatchGesture(new GestureDescription.Builder().addStroke(
                new GestureDescription.StrokeDescription(
                        basicPath.toPath(), 0,
                        MathUtil.clamp(basicPath.getDuration(), 1, Constants.MAX_GESTURE_DURATION) // precaution
                )
        ).build(), null, null);
    }

    private void adjustVolume(int direction) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                direction, AudioManager.FLAG_SHOW_UI);
    }

    private void lockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
        } else {
            new Handler(Looper.getMainLooper()).post(() ->
                    AndroidUtil.toast(getApplicationContext(), "This action can only be used in Android 9+"));
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

}
