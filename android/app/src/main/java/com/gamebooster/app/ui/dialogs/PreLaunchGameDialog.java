package com.gamebooster.app.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gamebooster.app.R;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.gamemanager.GameManagerLauncher;
import com.gamebooster.app.games.GameAppInfo;

/**
 * Pre-launch UI for selecting a display mode reported by Android before a game
 * is opened. The preference affects this app's display request only; games keep
 * control of their own graphics, frame pacing, and account data.
 */
public final class PreLaunchGameDialog {

    private static Dialog activeDialog;

    private PreLaunchGameDialog() {
    }

    public static void show(Context context, GameAppInfo game) {
        if (context == null || game == null) return;
        if (!(context instanceof Activity)) {
            GameManagerLauncher.launchGame(context, game);
            return;
        }

        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;

        dismissCurrent();
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_pre_launch_game, (ViewGroup) null, false);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.65f);
        }

        String packageName = game.getPackageName();
        String gameLabel = game.getLabel() == null ? packageName : game.getLabel();
        DevicePerformanceCapabilities capabilities = DevicePerformanceCapabilities.detect(context);

        ImageView icon = view.findViewById(R.id.iv_pre_launch_game_icon);
        TextView title = view.findViewById(R.id.tv_pre_launch_game_title);
        TextView pkg = view.findViewById(R.id.tv_pre_launch_game_pkg);
        TextView engine = view.findViewById(R.id.tv_pre_launch_game_engine);
        TextView badge = view.findViewById(R.id.tv_pre_launch_engine_badge);
        TextView display = view.findViewById(R.id.tv_pre_launch_display_hz);
        TextView autoFeatures = view.findViewById(R.id.tv_pre_launch_auto_features);

        if (game.getIcon() != null) icon.setImageDrawable(game.getIcon());
        title.setText(gameLabel);
        pkg.setText(packageName);
        engine.setText(detectGameEngineDescription(packageName));
        badge.setText("[ANDROID DISPLAY MODES]");
        badge.setTextColor(Color.parseColor("#00E5FF"));
        display.setText(describeDisplay(capabilities));
        autoFeatures.setText("• Uses only display modes reported by Android\n"
                + "• Stores a per-game display preference\n"
                + "• The game controls its own graphics and frame rate");

        RadioButton rate185 = view.findViewById(R.id.rb_pre_fps_185);
        RadioButton rate165 = view.findViewById(R.id.rb_pre_fps_165);
        RadioButton rate144 = view.findViewById(R.id.rb_pre_fps_144);
        RadioButton rate120 = view.findViewById(R.id.rb_pre_fps_120);
        configureRateButton(rate185, 185, capabilities);
        configureRateButton(rate165, 165, capabilities);
        configureRateButton(rate144, 144, capabilities);
        configureRateButton(rate120, 120, capabilities);
        showSystemDefaultWhenNeeded(rate185, rate165, rate144, rate120, capabilities);

        int savedRate = GameProfilePreferences.getTargetHz(context, packageName);
        int selectedRate = capabilities.resolveRefreshRate(savedRate);
        if (selectedRate <= 0) selectedRate = capabilities.getMaxRefreshRate();
        checkRateButton(selectedRate, rate185, 185);
        checkRateButton(selectedRate, rate165, 165);
        checkRateButton(selectedRate, rate144, 144);
        checkRateButton(selectedRate, rate120, 120);

        hideUnsupportedControls(view);

        Button cancel = view.findViewById(R.id.btn_pre_launch_cancel);
        Button start = view.findViewById(R.id.btn_pre_launch_start);
        cancel.setOnClickListener(ignored -> dismissCurrent());
        start.setOnClickListener(ignored -> {
            int requestedRate = selectedRate(rate185, rate165, rate144, rate120);
            int resolvedRate = capabilities.resolveRefreshRate(requestedRate);
            if (resolvedRate <= 0) {
                Toast.makeText(context, "No display mode is available from Android.", Toast.LENGTH_SHORT).show();
            } else {
                GameProfilePreferences.setTargetHz(context, packageName, resolvedRate);
            }
            dismissCurrent();
            GameManagerLauncher.launchGame(context, game);
        });

        Button remove = view.findViewById(R.id.btn_pre_launch_remove_game);
        if (remove != null) {
            remove.setOnClickListener(ignored -> confirmRemove(context, packageName, gameLabel));
        }

        dialog.setCanceledOnTouchOutside(true);
        activeDialog = dialog;
        dialog.show();
    }

    private static void configureRateButton(RadioButton button, int rate,
                                            DevicePerformanceCapabilities capabilities) {
        if (button == null) return;
        boolean available = capabilities.supportsRefreshRate(rate);
        button.setVisibility(available ? View.VISIBLE : View.GONE);
        button.setText("Use " + rate + "Hz display mode");
    }

    private static void checkRateButton(int selectedRate, RadioButton button, int buttonRate) {
        if (button != null && button.getVisibility() == View.VISIBLE && selectedRate == buttonRate) {
            button.setChecked(true);
        }
    }

    private static int selectedRate(RadioButton rate185, RadioButton rate165,
                                    RadioButton rate144, RadioButton rate120) {
        if (rate185 != null && rate185.isChecked()) return 185;
        if (rate165 != null && rate165.isChecked()) return 165;
        if (rate144 != null && rate144.isChecked()) return 144;
        if (rate120 != null && rate120.isChecked()) {
            Object requestedRate = rate120.getTag();
            return requestedRate instanceof Integer ? (Integer) requestedRate : 120;
        }
        return 0;
    }

    private static void showSystemDefaultWhenNeeded(RadioButton rate185, RadioButton rate165,
                                                    RadioButton rate144, RadioButton rate120,
                                                    DevicePerformanceCapabilities capabilities) {
        boolean hasDedicatedOption = (rate185 != null && rate185.getVisibility() == View.VISIBLE)
                || (rate165 != null && rate165.getVisibility() == View.VISIBLE)
                || (rate144 != null && rate144.getVisibility() == View.VISIBLE)
                || (rate120 != null && rate120.getVisibility() == View.VISIBLE);
        if (hasDedicatedOption || rate120 == null || capabilities.getMaxRefreshRate() <= 0) return;

        rate120.setVisibility(View.VISIBLE);
        rate120.setTag(Integer.valueOf(0));
        rate120.setText("Use system default (" + capabilities.getMaxRefreshRate() + "Hz maximum)");
        rate120.setChecked(true);
    }

    private static void hideUnsupportedControls(View view) {
        int[] controlIds = {
                R.id.switch_pre_launch_drone_view,
                R.id.layout_pre_launch_drone_view,
                R.id.layout_pre_launch_drone_tier,
                R.id.layout_pre_launch_hero_selector
        };
        for (int controlId : controlIds) {
            View control = view.findViewById(controlId);
            if (control != null) control.setVisibility(View.GONE);
        }
    }

    private static void confirmRemove(Context context, String packageName, String label) {
        dismissCurrent();
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Remove game")
                .setMessage("Remove " + label + " from the Home Launcher list?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    com.gamebooster.app.games.GameLauncherHelper.removeGameFromHome(context, packageName);
                    Toast.makeText(context, "Removed " + label + " from Home", Toast.LENGTH_SHORT).show();
                    if (context instanceof com.gamebooster.app.ui.activities.MainActivity) {
                        ((com.gamebooster.app.ui.activities.MainActivity) context).reloadHomeGames();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static int getDisplayMaxHz(Context context) {
        if (context == null) return 0;
        return DevicePerformanceCapabilities.detect(context).getMaxRefreshRate();
    }

    public static String detectGameEngineDescription(String packageName) {
        return "Android game • graphics and FPS are game-controlled";
    }

    private static String describeDisplay(DevicePerformanceCapabilities capabilities) {
        if (capabilities.getSupportedRefreshRates().isEmpty()) {
            return "Android did not report display modes";
        }
        return "Available display modes: " + capabilities.getSupportedRefreshRates();
    }

    public static void dismissCurrent() {
        if (activeDialog == null) return;
        try {
            if (activeDialog.isShowing()) activeDialog.dismiss();
        } catch (Throwable ignored) {
        }
        activeDialog = null;
    }
}
