package ma.portal.controller.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import ma.portal.controller.Constants;

public class BitmapUtil {
    private static final BitmapFactory.Options mutableOptions = new BitmapFactory.Options();
    static {
        mutableOptions.inMutable = true;
    }

    /**
     * Utility method to get pixels array from a Bitmap.
     */
    public static int[] getPixels(Bitmap b) {
        int w = b.getWidth();
        int h = b.getHeight();
        int[] data = new int[w * h];
        b.getPixels(data, 0, w, 0, 0, w, h);
        return data;
    }

    /**
     * Utility method to set pixels array of a Bitmap.
     */
    public static void setPixels(Bitmap b, int[] data) {
        int w = b.getWidth();
        int h = b.getHeight();
        b.setPixels(data, 0, w, 0, 0, w, h);
    }

    /**
     * Utility method to get a mutable Bitmap from a Base64 String.
     */
    public static Bitmap fromBase64(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, mutableOptions);
    }

    /**
     * Utility method to convert a Bitmap to JPEG Base64 String.
     */
    public static String toJPEGBase64(Bitmap bitmap, int quality) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            byte[] b = stream.toByteArray();

            return Base64.encodeToString(b, Base64.DEFAULT);
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * Utility method to return a String representing all changed pixels between two Bitmaps.
     *
     * @return null if the amount of changed pixels is greater than {@link Constants#SEND_CHANGED_PIXELS_THRESHOLD}, otherwise the String
     */
    @Nullable
    public static String getChangedPixelsStr(@Nullable Bitmap oldFrame, @NonNull Bitmap newFrame) {
        if (oldFrame == null) return null;

        final int w = oldFrame.getWidth();
        final int h = oldFrame.getHeight();
        int[] oldData = BitmapUtil.getPixels(oldFrame);
        int[] newData = BitmapUtil.getPixels(newFrame);

        int count = 0;
        StringBuilder changedPixels = new StringBuilder();

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                // Bitmap#getPixel is too slow
                int oldColor = oldData[x + y * w];
                int newColor = newData[x + y * w];

                if (count > Constants.SEND_CHANGED_PIXELS_THRESHOLD) {
                    return null;
                }

                if (oldColor != newColor) {
                    changedPixels.append(x)
                            .append(" ").append(y)
                            .append(" ").append(newColor)
                            .append('\n');
                    count++;
                }
            }
        }

        return changedPixels.toString();
    }

    public static Bitmap getBitmapFromImage(int displayWidth, int displayHeight, Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * displayWidth;

        Bitmap bitmap = Bitmap.createBitmap(displayWidth + rowPadding / pixelStride,
                displayHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        return bitmap;
    }
}
