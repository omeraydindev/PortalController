package ma.portal.controller.databinding;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import ma.portal.controller.R;

// (For the project to be portable to tools like Sketchware, CodeAssist etc.)
public class ActivityHostBinding {
    public Toolbar toolbar;
    public TextView txToken;
    public ImageView igStream;
    public ExtendedFloatingActionButton btnStartService;
    private View root;

    @SuppressLint("InflateParams")
    public static ActivityHostBinding inflate(LayoutInflater inflater) {
        ActivityHostBinding binding = new ActivityHostBinding();

        View root = inflater.inflate(R.layout.activity_host, null, false);
        binding.toolbar = root.findViewById(R.id.toolbar);
        binding.txToken = root.findViewById(R.id.tx_token);
        binding.igStream = root.findViewById(R.id.ig_stream);
        binding.btnStartService = root.findViewById(R.id.btn_start_service);
        binding.root = root;

        return binding;
    }

    public View getRoot() {
        return root;
    }
}
