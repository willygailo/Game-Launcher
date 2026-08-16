package com.gamebooster.app.core.profile;

import org.json.JSONObject;

/**
 * Model representing device-level touch, gyro, and pointer input tuning parameters.
 */
public class InputProfile {

    private String profileId;
    private String profileName;
    private String packageName;
    private int maxEventsPerSec;
    private int touchSlop;
    private int touchSlopReduction;
    private int gyroRate;
    private int pointerSpeed;
    private double pressureScale;

    public InputProfile(String profileId, String profileName, String packageName,
                        int maxEventsPerSec, int touchSlop, int touchSlopReduction,
                        int gyroRate, int pointerSpeed, double pressureScale) {
        this.profileId = profileId;
        this.profileName = profileName;
        this.packageName = packageName;
        this.maxEventsPerSec = maxEventsPerSec;
        this.touchSlop = touchSlop;
        this.touchSlopReduction = touchSlopReduction;
        this.gyroRate = gyroRate;
        this.pointerSpeed = pointerSpeed;
        this.pressureScale = pressureScale;
    }

    public String getProfileId() { return profileId; }
    public String getProfileName() { return profileName; }
    public String getPackageName() { return packageName; }
    public int getMaxEventsPerSec() { return maxEventsPerSec; }
    public int getTouchSlop() { return touchSlop; }
    public int getTouchSlopReduction() { return touchSlopReduction; }
    public int getGyroRate() { return gyroRate; }
    public int getPointerSpeed() { return pointerSpeed; }
    public double getPressureScale() { return pressureScale; }

    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("profileId", profileId);
            json.put("profileName", profileName);
            json.put("packageName", packageName);
            json.put("maxEventsPerSec", maxEventsPerSec);
            json.put("touchSlop", touchSlop);
            json.put("touchSlopReduction", touchSlopReduction);
            json.put("gyroRate", gyroRate);
            json.put("pointerSpeed", pointerSpeed);
            json.put("pressureScale", pressureScale);
            return json;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public static InputProfile fromJson(JSONObject json) {
        if (json == null) return null;
        return new InputProfile(
                json.optString("profileId", "default"),
                json.optString("profileName", "Default Profile"),
                json.optString("packageName", ""),
                json.optInt("maxEventsPerSec", 1000),
                json.optInt("touchSlop", 0),
                json.optInt("touchSlopReduction", 1),
                json.optInt("gyroRate", 1000),
                json.optInt("pointerSpeed", 7),
                json.optDouble("pressureScale", 0.0001)
        );
    }
}
