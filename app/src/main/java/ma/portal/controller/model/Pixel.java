package ma.portal.controller.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Pixel {
    @SerializedName("x")
    int x;
    @SerializedName("y")
    int y;
    @SerializedName("color")
    int color;

    public Pixel(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public static Pixel fromString(String pixelStr) {
        String[] comp = pixelStr.split(" ");
        return new Pixel(
                Integer.parseInt(comp[0]),
                Integer.parseInt(comp[1]),
                Integer.parseInt(comp[2])
        );
    }

    @NonNull
    @Override
    public String toString() {
        return x + " " + y + " " + color;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
