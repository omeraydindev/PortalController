package ma.portal.controller.model;

import com.google.gson.annotations.SerializedName;

public class ScreenPacket {

    @SerializedName("data")
    private Data data;
    @SerializedName("type")
    private String type;
    @SerializedName("token")
    private String token;

    public ScreenPacket(String type, String token, Data data) {
        this.data = data;
        this.type = type;
        this.token = token;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static class Data {
        @SerializedName("height")
        private int height;
        @SerializedName("width")
        private int width;
        @SerializedName("screen")
        private String screen;
        @SerializedName("pixels")
        private String pixels;

        public Data(String screen, String pixels, int width, int height) {
            this.height = height;
            this.width = width;
            this.screen = screen;
            this.pixels = pixels;
        }

        public String getPixels() {
            return pixels;
        }

        public void setPixels(String pixels) {
            this.pixels = pixels;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public String getScreen() {
            return screen;
        }

        public void setScreen(String screen) {
            this.screen = screen;
        }
    }
}
