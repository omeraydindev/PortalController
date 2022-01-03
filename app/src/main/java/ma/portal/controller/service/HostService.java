package ma.portal.controller.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ma.portal.controller.Constants;
import ma.portal.controller.Logs;
import ma.portal.controller.R;
import ma.portal.controller.util.BitmapUtil;
import ma.portal.controller.util.GzipUtil;

public class HostService extends Service {
    private final static int NOTIF_ID = 205;
    private final HostServiceBinder binder = new HostServiceBinder();
    private ImageListener imageListener;

    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private int displayWidth, displayHeight, densityDpi;

    @Override
    public void onCreate() {
        super.onCreate();

        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(metrics);
        displayWidth = metrics.widthPixels;
        displayHeight = metrics.heightPixels;
        densityDpi = metrics.densityDpi;
    }

    public void startRecording(int resultCode, Intent data, ImageListener imageListener) {
        this.imageListener = imageListener;

        MediaProjectionManager projectionManager = (MediaProjectionManager)
                getSystemService(MEDIA_PROJECTION_SERVICE);

        mediaProjection = projectionManager.getMediaProjection(resultCode, data);
        imageReader = createImageReader();
        virtualDisplay = createVirtualDisplay();
    }

    private VirtualDisplay createVirtualDisplay() {
        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

        return mediaProjection.createVirtualDisplay("screencap",
                displayWidth, displayHeight, densityDpi, flags,
                imageReader.getSurface(), null, null);
    }

    private ImageReader createImageReader() {
        @SuppressLint("WrongConstant") // PixelFormat.RGBA_8888 is the correct format but gotta please Lint.
        ImageReader imageReader = ImageReader.newInstance(
                displayWidth, displayHeight, PixelFormat.RGBA_8888, 1);

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            Bitmap lastBitmap;

            @Override
            public void onImageAvailable(ImageReader reader) {
                try (Image image = reader.acquireLatestImage()) {
                    if (image == null) return;
                    Logs.log("onImageAvailable");

                    Bitmap bitmap = BitmapUtil.getBitmapFromImage(displayWidth, displayHeight, image);
                    String changedPixels = BitmapUtil.getChangedPixelsStr(lastBitmap, bitmap);
                    lastBitmap = bitmap;

                    if (changedPixels != null) {
                        // if the amount of changed pixels are under the threshold, only send those
                        // and not the whole screen
                        String changedPixelsStr = GzipUtil.compress(changedPixels);
                        imageListener.onImageBitmap(bitmap, null, changedPixelsStr);
                    } else {
                        // send the whole screen in Base64, by first compressing it with JPEG
                        String base64Str = BitmapUtil.toJPEGBase64(bitmap, Constants.STREAM_QUALITY);
                        base64Str = GzipUtil.compress(base64Str);
                        imageListener.onImageBitmap(bitmap, base64Str, null);
                    }
                }
            }

        }, null);

        return imageReader;
    }

    public void stopRecording() {
        virtualDisplay.release();
        mediaProjection.stop();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = setupNotification();
        startForeground(NOTIF_ID, notification);

        return START_STICKY;
    }

    private Notification setupNotification() {
        return new NotificationCompat.Builder(this, createNotificationChannel())
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Record service")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();
    }

    private String createNotificationChannel() {
        NotificationChannelCompat channel = new NotificationChannelCompat.Builder("Recorder",
                NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("Recorder service")
                .setDescription("Foreground notification for the recorder service")
                .build();

        NotificationManagerCompat.from(this)
                .createNotificationChannel(channel);

        return "Recorder";
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public interface ImageListener {
        void onImageBitmap(Bitmap previewBitmap, String imageBase64, String changedPixels);
    }

    public class HostServiceBinder extends Binder {
        public HostService getService() {
            return HostService.this;
        }
    }
}
