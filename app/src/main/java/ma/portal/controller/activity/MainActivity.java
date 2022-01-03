package ma.portal.controller.activity;

import static ma.portal.controller.util.AndroidUtil.toast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ma.portal.controller.Constants;
import ma.portal.controller.adapter.HostsRVAdapter;
import ma.portal.controller.databinding.ActivityMainBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements Callback, HostsRVAdapter.HostClickListener {
    private final OkHttpClient client = new OkHttpClient();
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadHosts();
        binding.swipeRefresh.setOnRefreshListener(this::loadHosts);
        binding.btnHost.setOnClickListener(v -> {
            Intent intent = new Intent(this, HostActivity.class);
            startActivity(intent);
        });
    }

    private void loadHosts() {
        binding.swipeRefresh.setRefreshing(true);

        Request request = new Request.Builder()
                .get()
                .url(Constants.HOSTS_URL)
                .build();

        client.newCall(request).enqueue(this);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) {
        runOnUiThread(() -> {
            try {
                JSONArray array = new JSONArray(response.body().string());
                List<String> hosts = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    String host = array.optString(i);
                    if (!hosts.contains(host)) hosts.add(host);
                }

                HostsRVAdapter adapter = new HostsRVAdapter(this, hosts);
                adapter.setHostClickListener(this);

                binding.rvHosts.setAdapter(adapter);
                binding.rvHosts.setLayoutManager(new LinearLayoutManager(this));
                binding.rvHosts.setVisibility(hosts.size() > 0 ? View.VISIBLE : View.GONE);
            } catch (Exception e) {
                toast(getApplicationContext(), "Can't parse response: " + e.toString());
            }

            binding.swipeRefresh.setRefreshing(false);
        });
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        runOnUiThread(() -> {
            toast(getApplicationContext(), "Request error: " + e.toString());
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    @Override
    public void onClick(String host) {
        Intent intent = new Intent(this, ControlActivity.class);
        intent.putExtra("host", host);
        startActivity(intent);
    }
}
