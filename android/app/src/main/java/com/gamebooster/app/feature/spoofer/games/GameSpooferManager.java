package com.gamebooster.app.feature.spoofer.games;

import android.content.Context;
import android.util.Log;

public class GameSpooferManager {

    private static final String TAG = "GameSpooferManager";

    private static GameSpooferManager instance;

    private GameSpooferManager() {}

    public static synchronized GameSpooferManager getInstance() {
        if (instance == null) {
            instance = new GameSpooferManager();
        }
        return instance;
    }

    public GameSpooferInterface getStrategyForPackage(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return new GenericGameSpooferStrategy();
        }

        String pkg = packageName.toLowerCase().trim();
        Log.d(TAG, "Resolving game spoofer strategy for package: " + pkg);

        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            return new MlbbSpooferStrategy();
        } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile")) {
            return new PubgSpooferStrategy();
        } else if (pkg.contains("cod") || pkg.contains("callofduty")) {
            return new CodmSpooferStrategy();
        } else if (pkg.contains("sgame") || pkg.contains("levelinfinite")) {
            return new HokSpooferStrategy();
        } else if (pkg.contains("genshin") || pkg.contains("hkrpg")) {
            return new GenshinStarRailSpooferStrategy();
        } else if (pkg.contains("freefire")) {
            return new FreeFireSpooferStrategy();
        } else if (pkg.contains("wildrift")) {
            return new WildRiftSpooferStrategy();
        } else if (pkg.contains("nap") || pkg.contains("wutheringwaves") || pkg.contains("kurogame")) {
            return new ZzzWuWaSpooferStrategy();
        } else if (pkg.contains("dfm") || pkg.contains("deltaforce")) {
            return new DeltaForceSpooferStrategy();
        } else {
            return new GenericGameSpooferStrategy();
        }
    }

    public boolean applySpoofForPackage(Context context, String packageName) {
        GameSpooferInterface strategy = getStrategyForPackage(packageName);
        Log.i(TAG, "Executing Spoofer Strategy: " + strategy.getStrategyName() + " for package " + packageName);
        return strategy.applyGameSpoof(context, packageName);
    }
}
