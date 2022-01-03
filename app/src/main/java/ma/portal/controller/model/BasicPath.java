package ma.portal.controller.model;

import android.graphics.Path;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BasicPath {
    @SerializedName("steps")
    private final List<Step> steps;

    @SerializedName("duration")
    private long duration;

    public BasicPath() {
        steps = new ArrayList<>();
    }

    public void reset() {
        steps.clear();
    }

    public void moveTo(float x, float y) {
        steps.add(new Step(x, y));
    }

    public void lineTo(float x, float y) {
        steps.add(new Step(x, y));
    }

    public Path toPath() {
        Path path = new Path();

        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            if (i == 0) {
                path.moveTo(step.x, step.y);
            } else {
                path.lineTo(step.x, step.y);
            }
        }

        return path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    private static class Step {
        @SerializedName("x")
        float x;

        @SerializedName("y")
        float y;

        public Step(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

}
