package com.gamebooster.app;

import com.gamebooster.app.booster.refreshrate.RefreshRateInterface;
import com.gamebooster.app.booster.refreshrate.RefreshRateManager;
import com.gamebooster.app.booster.thermal.ThermalInterface;
import com.gamebooster.app.booster.thermal.ThermalManager;
import com.gamebooster.app.bypasscharging.BypassChargingInterface;
import com.gamebooster.app.bypasscharging.BypassChargingManager;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.spoofer.SpoofProfile;
import com.gamebooster.app.spoofer.SpoofProfileRegistry;
import com.gamebooster.app.spoofer.games.GameSpooferInterface;
import com.gamebooster.app.spoofer.games.GameSpooferManager;
import com.gamebooster.app.tweaks.TweakItem;
import com.gamebooster.app.tweaks.TweakManagerRepository;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FolderFilesRegistryTest {

    @Test
    public void testSpoofProfileRegistryAllBrandsRegistered() {
        int totalCount = SpoofProfileRegistry.getTotalCount();
        Assert.assertTrue("Total registered spoof profiles should be > 0", totalCount >= 30);

        boolean integrity = SpoofProfileRegistry.validateRegistryIntegrity();
        Assert.assertTrue("All registered brand profiles must pass integrity check", integrity);

        List<String> brandNames = SpoofProfileRegistry.getBrandNames();
        Assert.assertNotNull(brandNames);
        Assert.assertTrue("Brand names should include Samsung", brandNames.contains("Samsung"));
        Assert.assertTrue("Brand names should include Xiaomi", brandNames.contains("Xiaomi"));
        Assert.assertTrue("Brand names should include ASUS ROG", brandNames.contains("ASUS ROG"));
        Assert.assertTrue("Brand names should include Apple", brandNames.contains("Apple"));

        Map<String, SpoofProfile> allMap = SpoofProfileRegistry.getAllProfiles();
        Assert.assertEquals(totalCount, allMap.size());
        for (Map.Entry<String, SpoofProfile> entry : allMap.entrySet()) {
            Assert.assertNotNull(entry.getKey());
            Assert.assertNotNull(entry.getValue());
            Assert.assertEquals(entry.getKey(), entry.getValue().id);
        }
    }

    @Test
    public void testGameSpooferManagerStrategyResolution() {
        GameSpooferManager manager = GameSpooferManager.getInstance();
        Assert.assertNotNull(manager);

        // Registered games
        GameSpooferInterface mlbbStrategy = manager.getStrategyForPackage("com.mobile.legends");
        Assert.assertNotNull(mlbbStrategy);
        Assert.assertNotNull(mlbbStrategy.getStrategyName());
        Assert.assertNotNull(mlbbStrategy.getSpoofProfile());

        GameSpooferInterface pubgStrategy = manager.getStrategyForPackage("com.tencent.ig");
        Assert.assertNotNull(pubgStrategy);

        GameSpooferInterface codmStrategy = manager.getStrategyForPackage("com.activision.callofduty.shooter");
        Assert.assertNotNull(codmStrategy);

        GameSpooferInterface hokStrategy = manager.getStrategyForPackage("com.levelinfinite.sgameGlobal");
        Assert.assertNotNull(hokStrategy);

        GameSpooferInterface genshinStrategy = manager.getStrategyForPackage("com.miHoYo.GenshinImpact");
        Assert.assertNotNull(genshinStrategy);

        // Fallback for unknown package
        GameSpooferInterface unknownStrategy = manager.getStrategyForPackage("com.unknown.randomgame");
        Assert.assertNotNull(unknownStrategy);
        Assert.assertTrue(unknownStrategy.getStrategyName().contains("Generic Game Fallback Spoofer"));
    }


    @Test
    public void testBypassChargingManagerStrategies() {
        BypassChargingManager manager = BypassChargingManager.getInstance();
        Assert.assertNotNull(manager);

        BypassChargingInterface strategy = manager.getCurrentStrategy();
        Assert.assertNotNull("Current Bypass Charging strategy must not be null", strategy);
        Assert.assertNotNull("Strategy name must not be null", strategy.getStrategyName());
    }

    @Test
    public void testRefreshRateManagerStrategies() {
        RefreshRateManager manager = RefreshRateManager.getInstance();
        Assert.assertNotNull(manager);

        RefreshRateInterface strategy = manager.getCurrentStrategy();
        Assert.assertNotNull("Current Refresh Rate strategy must not be null", strategy);
        Assert.assertNotNull("Strategy name must not be null", strategy.getStrategyName());
    }

    @Test
    public void testThermalManagerStrategies() {
        ThermalManager manager = ThermalManager.getInstance();
        Assert.assertNotNull(manager);

        ThermalInterface strategy = manager.getCurrentStrategy();
        Assert.assertNotNull("Current Thermal strategy must not be null", strategy);
        Assert.assertNotNull("Strategy name must not be null", strategy.getStrategyName());
    }

    @Test
    public void testTweakManagerRepositoryIntegrity() {
        List<TweakItem> tweaks = TweakManagerRepository.getAllTweaks();
        Assert.assertNotNull(tweaks);
        Assert.assertTrue("Tweaks repository must contain items", tweaks.size() > 10);

        Set<String> ids = new HashSet<>();
        for (TweakItem tweak : tweaks) {
            Assert.assertNotNull(tweak.getId());
            Assert.assertNotNull(tweak.getTitle());
            Assert.assertNotNull(tweak.getApplyCommand());
            Assert.assertNotNull(tweak.getRevertCommand());
            Assert.assertNotNull(tweak.getCategory());
            Assert.assertTrue("Tweak ID must be unique: " + tweak.getId(), ids.add(tweak.getId().toLowerCase()));
        }
    }

    @Test
    public void testGamePackageRegistryAllGames() {
        Map<String, GamePackageRegistry.GameInfoSpec> knownGames = GamePackageRegistry.getAllKnownGames();
        Assert.assertNotNull(knownGames);
        Assert.assertTrue("Known games registry should contain entries", knownGames.size() >= 30);

        for (Map.Entry<String, GamePackageRegistry.GameInfoSpec> entry : knownGames.entrySet()) {
            Assert.assertNotNull("Package name key must not be null", entry.getKey());
            GamePackageRegistry.GameInfoSpec spec = entry.getValue();
            Assert.assertNotNull("Game spec must not be null for " + entry.getKey(), spec);
            Assert.assertNotNull("Game title must not be null", spec.title);
            Assert.assertNotNull("Game category must not be null", spec.category);
            Assert.assertTrue("Max supported FPS must be > 0", spec.maxSupportedFps > 0);
        }
    }

    @Test
    public void testShizukuBatchCommandsFormatting() {
        java.util.List<String> list = java.util.Arrays.asList(
                "pm grant com.test android.permission.WRITE_SETTINGS",
                "cmd appops set com.test MANAGE_EXTERNAL_STORAGE allow",
                "cmd game mode performance com.test"
        );
        String res = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuBatchCommands(list);
        Assert.assertNotNull(res);
    }
}

