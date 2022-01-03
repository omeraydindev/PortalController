package ma.portal.controller.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.widget.Toast;

import java.util.UUID;

public class AndroidUtil {
    public static void toast(Context context, int strId) {
        Toast.makeText(context, strId, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static float dp(Context context, int dp) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static String getToken() {
        return (Build.MANUFACTURER + "_" + Build.MODEL + "_" + UUID.randomUUID().toString().substring(0, 4))
                .replace(" ", "_");
    }
}
