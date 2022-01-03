package ma.portal.controller.databinding;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ma.portal.controller.R;

// (For the project to be portable to tools like Sketchware, CodeAssist etc.)
public class ActivityControlBinding {
    public TextView txToken;
    public ImageView screen;
    public LinearLayout lnButtons;
    public ImageButton btnBack;
    public ImageButton btnHome;
    public ImageButton btnRecents;
    public ImageButton btnVoldown;
    public ImageButton btnVolup;
    public ImageButton btnLockscr;
    private View root;

    @SuppressLint("InflateParams")
    public static ActivityControlBinding inflate(LayoutInflater inflater) {
        ActivityControlBinding binding = new ActivityControlBinding();

        View root = inflater.inflate(R.layout.activity_control, null, false);
        binding.txToken = root.findViewById(R.id.tx_token);
        binding.screen = root.findViewById(R.id.screen);
        binding.lnButtons = root.findViewById(R.id.ln_buttons);
        binding.btnBack = root.findViewById(R.id.btn_back);
        binding.btnHome = root.findViewById(R.id.btn_home);
        binding.btnRecents = root.findViewById(R.id.btn_recents);
        binding.btnVoldown = root.findViewById(R.id.btn_voldown);
        binding.btnVolup = root.findViewById(R.id.btn_volup);
        binding.btnLockscr = root.findViewById(R.id.btn_lockscr);
        binding.root = root;

        return binding;
    }

    public View getRoot() {
        return root;
    }
}
