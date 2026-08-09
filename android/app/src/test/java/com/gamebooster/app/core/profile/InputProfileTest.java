package com.gamebooster.app.core.profile;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class InputProfileTest {

    @Test
    public void testInputProfile_SerializationDeserialization() throws JSONException {
        InputProfile original = new InputProfile(
                "test_pubg",
                "PUBG Test Profile",
                "com.tencent.ig",
                1000,
                0,
                1,
                1000,
                7,
                0.0001
        );

        JSONObject json = original.toJson();
        Assert.assertNotNull(json);
        Assert.assertEquals("test_pubg", json.getString("profileId"));
        Assert.assertEquals(1000, json.getInt("maxEventsPerSec"));

        InputProfile restored = InputProfile.fromJson(json);
        Assert.assertNotNull(restored);
        Assert.assertEquals(original.getProfileId(), restored.getProfileId());
        Assert.assertEquals(original.getProfileName(), restored.getProfileName());
        Assert.assertEquals(original.getPackageName(), restored.getPackageName());
        Assert.assertEquals(original.getMaxEventsPerSec(), restored.getMaxEventsPerSec());
        Assert.assertEquals(original.getTouchSlop(), restored.getTouchSlop());
        Assert.assertEquals(original.getGyroRate(), restored.getGyroRate());
        Assert.assertEquals(original.getPointerSpeed(), restored.getPointerSpeed());
        Assert.assertEquals(original.getPressureScale(), restored.getPressureScale(), 0.00001);
    }
}
