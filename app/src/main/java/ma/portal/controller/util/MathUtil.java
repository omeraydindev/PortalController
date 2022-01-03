package ma.portal.controller.util;

public class MathUtil {

    public static float clamp(float value, float minimum, float maximum) {
        return Math.min(maximum, Math.max(minimum, value));
    }

    public static long clamp(long value, long minimum, long maximum) {
        return Math.min(maximum, Math.max(minimum, value));
    }

    public static float map(float n, float start1, float stop1, float start2, float stop2) {
        return ((n - start1) / (stop1 - start1)) * (stop2 - start2) + start2;
    }

}
