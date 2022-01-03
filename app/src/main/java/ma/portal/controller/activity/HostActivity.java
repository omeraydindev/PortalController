package ma.portal.controller.activity;

import static android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import org.json.JSONObject;

import ma.portal.controller.Constants;
import ma.portal.controller.Logs;
import ma.portal.controller.R;
import ma.portal.controller.databinding.ActivityHostBinding;
import ma.portal.controller.model.ControlPacket;
import ma.portal.controller.model.ScreenPacket;
import ma.portal.controller.service.DeviceControllerService;
import ma.portal.controller.service.HostService;
import ma.portal.controller.service.HostServiceConnection;
import ma.portal.controller.util.AccessibilityUtil;
import ma.portal.controller.util.AndroidUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class HostActivity extends AppCompatActivity {
    private static final int SCREEN_CAPTURE_REQ = 193;
    private static final String token = AndroidUtil.getToken();

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private ActivityHostBinding binding;
    private HostServiceConnection recordConnection;
    private WebSocket ws;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        scalePreview();
        binding.txToken.setText(token);
        binding.btnStartService.setOnClickListener(v -> {
            String btnText = binding.btnStartService.getText().toString();

            if (btnText.equals(getString(R.string.start_service))) {
                startService();
            } else {
                stopService();
            }
        });
    }

    private void scalePreview() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        binding.igStream.setLayoutParams(new LinearLayout.LayoutParams(
                metrics.widthPixels / 3,
                metrics.heightPixels / 3
        ));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectFromWs();
    }

    private void startService() {
        if (!AccessibilityUtil.isServiceEnabled(this, DeviceControllerService.class)) {
            AndroidUtil.toast(this, R.string.acc_message);
            startActivity(new Intent(ACTION_ACCESSIBILITY_SETTINGS));
            return;
        }

        binding.btnStartService.setText(R.string.stop_service);
        binding.btnStartService.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_stop_24));

        requestScreenCapture();
    }

    private void stopService() {
        binding.btnStartService.setText(R.string.start_service);
        binding.btnStartService.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_play_arrow_24));

        if (recordConnection != null) {
            recordConnection.stopRecording();
        }

        disconnectFromWs();
    }

    private void requestScreenCapture() {
        MediaProjectionManager projectionManager = (MediaProjectionManager)
                getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_CAPTURE_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCREEN_CAPTURE_REQ && resultCode == RESULT_OK) {
            Logs.clear();
            Logs.log("onActivityResult OK");
            connectToWs(() -> {
                recordConnection = new HostServiceConnection(resultCode, data, (previewBitmap, imageBase64, changedPixels) -> {
                    runOnUiThread(() -> binding.igStream.setImageBitmap(previewBitmap));
                    sendScreen(imageBase64, changedPixels, previewBitmap.getWidth(), previewBitmap.getHeight());
                });

                startService(new Intent(this, HostService.class));
                bindService(new Intent(this, HostService.class),
                        recordConnection, BIND_IMPORTANT);
            });
        }
    }

    private void connectToWs(OnWsOpenListener onWsOpenListener) {
        Request request = new Request.Builder()
                .get()
                .url(Constants.WS_URL + "?token=" + token)
                .build();

        ws = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                Logs.log("ws onOpen");
                onWsOpenListener.onOpen();
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                Logs.log("ws onMessage");
                onControlPacket(text);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                Logs.log("ws onMessage ByteString");
                onControlPacket(bytes.utf8());
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                Logs.log("ws onFailure " + t.toString());
                runOnUiThread(() -> {
                    AndroidUtil.toast(getApplicationContext(), "WebSocket connection failed: " + t.toString());
                    stopService();
                });
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Logs.log("ws onClosing " + code + ":" + reason);
                runOnUiThread(HostActivity.this::stopService);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Logs.log("ws onClosed " + code + ":" + reason);
                runOnUiThread(HostActivity.this::stopService);
            }
        });
    }

    private void disconnectFromWs() {
        if (ws != null) {
            ws.cancel();
        }
    }

    private void onControlPacket(String json) {
        try {
            JSONObject packet = new JSONObject(json);
            if (!packet.optString("type").equals("controller")) {
                return;
            }
            if (!packet.optString("token").equals(token)) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        ControlPacket controlPacket = new Gson().fromJson(json, ControlPacket.class);

        DeviceControllerService controller = DeviceControllerService.instance;
        if (controller != null) {
            Logs.log("ws onControlPacket: " + controlPacket.getData().getAction());
            controller.process(controlPacket);
        }
    }

    private void sendScreen(String imageBase64, String changedPixels, int width, int height) {
        if (imageBase64 != null) {
            Logs.log("ws sendScreen imageBase64:" + imageBase64.length() + " " + width + "x" + height);
        } else if (changedPixels != null) {
            Logs.log("ws sendScreen changedPixels:" + changedPixels.length() + " " + width + "x" + height);
        }
        ScreenPacket screenPacket =
                new ScreenPacket("host", token,
                        new ScreenPacket.Data(imageBase64, changedPixels, width, height));

        ws.send(gson.toJson(screenPacket));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Logs");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals("Logs")) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("", Logs.get()));

            AndroidUtil.toast(this, "Copied logs to clipboard");
            return true;
        }
        return false;
    }

    private interface OnWsOpenListener {
        void onOpen();
    }
}
