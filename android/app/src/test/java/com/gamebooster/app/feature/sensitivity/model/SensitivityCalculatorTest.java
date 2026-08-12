package com.gamebooster.app.feature.sensitivity.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class SensitivityCalculatorTest {

    @Test
    public void testCalculateSensitivityStandard() {
        SensitivityModel model = SensitivityCalculator.calculate(400, 6.5, 1.0f);
        assertNotNull(model);
        assertTrue(model.freeLook > 0);
        assertTrue(model.noScope3rdPerson > 0);
        assertTrue(model.gyroNoScope > 0);
        assertNotNull(model.summary);
    }

    @Test
    public void testCalculateSensitivityHighDpi() {
        SensitivityModel model = SensitivityCalculator.calculate(800, 6.5, 1.5f);
        assertNotNull(model);
        assertTrue(model.noScope3rdPerson <= 300);
        assertTrue(model.gyroNoScope <= 400);
    }
}
