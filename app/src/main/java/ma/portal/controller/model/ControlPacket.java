package ma.portal.controller.model;

import com.google.gson.annotations.SerializedName;

public class ControlPacket {

    @SerializedName("data")
    private Data data;
    @SerializedName("type")
    private String type;
    @SerializedName("token")
    private String token;

    public ControlPacket(String type, String token, Data data) {
        this.data = data;
        this.type = type;
        this.token = token;
    }

    public Data getData() {
        return data;
    }

    public String getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public static class Data {
        public static final String ACTION_GESTURE = "gesture";
        public static final String ACTION_BACK = "back";
        public static final String ACTION_HOME = "home";
        public static final String ACTION_RECENTS = "recents";
        public static final String ACTION_VOLUP = "volup";
        public static final String ACTION_VOLDOWN = "voldown";
        public static final String ACTION_LOCKSCR = "lockscr";

        @SerializedName("action")
        private String action;
        @SerializedName("gesture")
        private BasicPath gesture;

        public Data(String action, BasicPath gesture) {
            this.action = action;
            this.gesture = gesture;
        }

        public String getAction() {
            return action;
        }

        public BasicPath getGesture() {
            return gesture;
        }
    }
}
