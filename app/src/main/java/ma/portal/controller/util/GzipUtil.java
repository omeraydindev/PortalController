package ma.portal.controller.util;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import kotlin.io.TextStreamsKt;

public class GzipUtil {

    /**
     * Decodes Base64 String, then decompresses it using GZIP.
     *
     * @return Returns null if an IOException occurred.
     */
    public static String decompress(String compressedBase64) {
        byte[] bytes = Base64.decode(compressedBase64, Base64.DEFAULT);

        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
             BufferedReader bf = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8))) {

            return TextStreamsKt.readText(bf);

        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Compresses String using GZIP then returns it in Base64.
     */
    public static String compress(String str) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
                gzip.write(str.getBytes(StandardCharsets.UTF_8));
            }

            return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
        } catch (IOException e) {
            return null;
        }
    }

}
