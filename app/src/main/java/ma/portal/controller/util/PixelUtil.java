package ma.portal.controller.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import ma.portal.controller.model.Pixel;

public class PixelUtil {

    public static List<Pixel> fromString(String pixelsStr) {
        List<Pixel> pixels = new ArrayList<>();
        pixelsStr = GzipUtil.decompress(pixelsStr);

        try (BufferedReader buf = new BufferedReader(new StringReader(pixelsStr))) {
            String line;
            while ((line = buf.readLine()) != null) {
                pixels.add(Pixel.fromString(line));
            }
        } catch (IOException ignored) {
        }

        return pixels;
    }

}
