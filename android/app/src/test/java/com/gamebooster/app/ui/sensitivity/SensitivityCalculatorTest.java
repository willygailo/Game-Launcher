package com.gamebooster.app.ui.sensitivity;

import org.junit.Assert;
import org.junit.Test;

public class SensitivityCalculatorTest {

    @Test
    public void testSensitivityCalculation_StandardPhone() {
        int dpi = 400;
        double screenSize = 6.5;
        float gyroMultiplier = 1.0f;

        SensitivityModel model = SensitivityCalculator.calculate(dpi, screenSize, gyroMultiplier);

        Assert.assertNotNull(model);
        Assert.assertTrue(model.freeLook > 0 && model.freeLook <= 300);
        Assert.assertTrue(model.gyroNoScope > 0 && model.gyroNoScope <= 400);
        Assert.assertTrue(model.summary.contains("400 DPI"));
    }

    @Test
    public void testSensitivityCalculation_HighDpiTablet() {
        int dpi = 600;
        double screenSize = 11.0;
        float gyroMultiplier = 1.5f;

        SensitivityModel model = SensitivityCalculator.calculate(dpi, screenSize, gyroMultiplier);

        Assert.assertNotNull(model);
        Assert.assertTrue(model.noScope3rdPerson >= 1);
        Assert.assertTrue(model.gyroRedDot >= 1);
        Assert.assertTrue(model.summary.contains("600 DPI"));
    }

    @Test
    public void testSensitivityClamping() {
        // Extreme DPI values should be safely clamped between 1 and 300 / 400
        SensitivityModel modelLow = SensitivityCalculator.calculate(10000, 2.0, 0.1f);
        Assert.assertTrue(modelLow.freeLook >= 1);

        SensitivityModel modelHigh = SensitivityCalculator.calculate(10, 20.0, 10.0f);
        Assert.assertTrue(modelHigh.freeLook <= 300);
        Assert.assertTrue(modelHigh.gyroNoScope <= 400);
    }
}
