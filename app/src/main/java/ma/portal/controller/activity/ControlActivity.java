package ma.portal.controller.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.List;

import ma.portal.controller.Constants;
import ma.portal.controller.databinding.ActivityControlBinding;
import ma.portal.controller.model.BasicPath;
import ma.portal.controller.model.ControlPacket;
import ma.portal.controller.model.Pixel;
import ma.portal.controller.model.ScreenPacket;
import ma.portal.controller.util.AndroidUtil;
import ma.portal.controller.util.BitmapUtil;
import ma.portal.controller.util.GzipUtil;
import ma.portal.controller.util.MathUtil;
import ma.portal.controller.util.PixelUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class ControlActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private ActivityControlBinding binding;

    private String host;
    private boolean scaledScreen;
    private WebSocket ws;

    private int remoteWidth, remoteHeight, windowWidth, windowHeight;
    private Bitmap lastBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityControlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        host = getIntent().getStringExtra("host");
        binding.txToken.setText(host);
    }

    @Override
    protected void onStart() {
        super.onStart();
        connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        disconnect();
    }

    private void connect() {
        Request request = new Request.Builder()
                .get()
                .url(Constants.WS_URL + "?token=" + host)
                .build();

        ws = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                onScreenPacket(bytes.utf8());
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                onScreenPacket(text);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                runOnUiThread(() ->
                        AndroidUtil.toast(getApplicationContext(), "WebSocket connection failed: " + t.toString()));
            }
        });

        initControls();
    }

    private void onScreenPacket(String json) {
        ScreenPacket screenPacket = gson.fromJson(json, ScreenPacket.class);

        if (!host.equals(screenPacket.getToken())) {
            return;
        }

        String changedPixelsStr = screenPacket.getData().getPixels();
        String imageBase64Str = screenPacket.getData().getScreen();

        // if the packet has `changed pixels` and we have the last frame,
        // just replace pixels of the last frame using the `changed pixels`
        if (changedPixelsStr != null && lastBitmap != null) {
            List<Pixel> changedPixels = PixelUtil.fromString(changedPixelsStr);

            int w = lastBitmap.getWidth();
            int[] lastBitmapPixels = BitmapUtil.getPixels(lastBitmap);

            for (Pixel pixel : changedPixels) {
                int x = pixel.getX();
                int y = pixel.getY();
                int color = pixel.getColor();

                lastBitmapPixels[x + y * w] = color; // Bitmap#setPixel is too slow
            }
            BitmapUtil.setPixels(lastBitmap, lastBitmapPixels);

        } else if (imageBase64Str != null) { // if we have the Base64 of the whole pic instead, use that
            lastBitmap = BitmapUtil.fromBase64(GzipUtil.decompress(imageBase64Str));
        }

        runOnUiThread(() -> {
            if (!scaledScreen) { // scale the stream window
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int screenHeight = metrics.heightPixels;

                remoteWidth = screenPacket.getData().getWidth();
                remoteHeight = screenPacket.getData().getHeight();

                windowHeight = screenHeight - binding.lnButtons.getHeight() - binding.txToken.getHeight()
                        - (int) AndroidUtil.dp(this, 50);
                windowWidth = (int) (remoteWidth * ((float) windowHeight / remoteHeight));

                binding.screen.setLayoutParams(new LinearLayout.LayoutParams(windowWidth, windowHeight));
                scaledScreen = true;
            }

            binding.screen.setImageBitmap(lastBitmap);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initControls() {
        binding.screen.setOnTouchListener(new View.OnTouchListener() {
            private final BasicPath tempPath = new BasicPath();
            private long tempMillis;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = MathUtil.map(MathUtil.clamp(event.getX(), 0, windowWidth),
                        0, windowWidth, 0, remoteWidth);
                float y = MathUtil.map(MathUtil.clamp(event.getY(), 0, windowHeight),
                        0, windowHeight, 0, remoteHeight);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        tempMillis = System.currentTimeMillis();

                        tempPath.reset();
                        tempPath.moveTo(x, y);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        tempPath.lineTo(x, y);
                        return true;

                    case MotionEvent.ACTION_UP:
                        tempMillis = System.currentTimeMillis() - tempMillis;

                        tempPath.lineTo(x, y);

                        tempPath.setDuration(MathUtil.clamp(tempMillis, 1, Constants.MAX_GESTURE_DURATION));
                        // send the gesture packet once the user releases their finger
                        sendControlPacket(tempPath);
                        return true;
                }
                return false;
            }
        });

        binding.btnBack.setOnClickListener(v -> sendControlPacket(ControlPacket.Data.ACTION_BACK));
        binding.btnHome.setOnClickListener(v -> sendControlPacket(ControlPacket.Data.ACTION_HOME));
        binding.btnRecents.setOnClickListener(v -> sendControlPacket(ControlPacket.Data.ACTION_RECENTS));
        binding.btnVoldown.setOnClickListener(v -> sendControlPacket(ControlPacket.Data.ACTION_VOLDOWN));
        binding.btnVolup.setOnClickListener(v -> sendControlPacket(ControlPacket.Data.ACTION_VOLUP));
        binding.btnLockscr.setOnClickListener(v -> sendControlPacket(ControlPacket.Data.ACTION_LOCKSCR));
    }

    private void sendControlPacket(BasicPath basicPath) {
        if (ws == null) return;

        ControlPacket controlPacket =
                new ControlPacket("controller", host,
                        new ControlPacket.Data(ControlPacket.Data.ACTION_GESTURE, basicPath));

        ws.send(gson.toJson(controlPacket));
    }

    private void sendControlPacket(String action) {
        if (ws == null) return;

        ControlPacket controlPacket =
                new ControlPacket("controller", host,
                        new ControlPacket.Data(action, null));

        ws.send(gson.toJson(controlPacket));
    }

    private void disconnect() {
        ws.cancel();
    }
}
