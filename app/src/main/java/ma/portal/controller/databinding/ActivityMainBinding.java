package ma.portal.controller.databinding;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import ma.portal.controller.R;

// (For the project to be portable to tools like Sketchware, CodeAssist etc.)
public class ActivityMainBinding {
    public SwipeRefreshLayout swipeRefresh;
    public RecyclerView rvHosts;
    public CardView btnHost;
    private View root;

    @SuppressLint("InflateParams")
    public static ActivityMainBinding inflate(LayoutInflater inflater) {
        ActivityMainBinding binding = new ActivityMainBinding();

        View root = inflater.inflate(R.layout.activity_main, null, false);
        binding.swipeRefresh = root.findViewById(R.id.swipe_refresh);
        binding.rvHosts = root.findViewById(R.id.rv_hosts);
        binding.btnHost = root.findViewById(R.id.btn_host);
        binding.root = root;

        return binding;
    }

    public View getRoot() {
        return root;
    }
}
