package com.gamebooster.app.ui.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.config.*;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameLauncherHelper;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.ArrayList;
import java.util.List;

public class HomeGamesAdapter extends RecyclerView.Adapter<HomeGamesAdapter.GameViewHolder> {

    /**
     * Sentinel value used in the FPS radio picker to represent the
     * "144fps UltraExtreme SuperSmooth" preset. When selected, the adapter
     * calls {@link GameConfigPatcher#applyUltraExtreme144Patch(android.content.Context, String)}
     * instead of the generic FPS patcher.
     */
    private static final int FPS_ULTRA_EXTREME_SENTINEL = -144;

    private final Context context;
    private final List<GameAppInfo> games = new ArrayList<>();

    public HomeGamesAdapter(Context context, List<GameAppInfo> initialGames) {
        this.context = context;
        if (initialGames != null) {
            this.games.addAll(initialGames);
        }
    }

    public void updateList(List<GameAppInfo> newList) {
        games.clear();
        if (newList != null) {
            games.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_home_game_card, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameAppInfo game = games.get(position);
        holder.tvTitle.setText(game.getLabel());
        holder.tvPkg.setText(game.getPackageName());
        holder.tvProfile.setText(GameProfilePreferences.getSummary(context, game.getPackageName()));
        
        if (game.getIcon() != null) {
            holder.ivIcon.setImageDrawable(game.getIcon());
        }

        // Apply Game-Specific Badge & Background Accent
        if (game.getGameType() != null) {
            holder.tvBadge.setText(game.getGameType());
        }
        if (game.getBadgeColor() != 0) {
            holder.tvBadge.setTextColor(game.getBadgeColor());
        }
        if (game.getCardBgRes() != 0) {
            holder.layoutCardBg.setBackgroundResource(game.getCardBgRes());
        }

        View.OnClickListener launchListener = v -> com.gamebooster.app.gamemanager.GameManagerLauncher.launchGame(context, game);
        holder.btnLaunch.setOnClickListener(launchListener);
        if (holder.layoutCardBg != null) {
            holder.layoutCardBg.setOnClickListener(launchListener);
        }
        holder.itemView.setOnClickListener(launchListener);
        holder.btnConfig.setOnClickListener(v -> showProfilePicker(holder, game));
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    private void showProfilePicker(GameViewHolder holder, GameAppInfo game) {
        String pkg = game.getPackageName().toLowerCase();

        // ── Game key resolution ──────────────────────────────────────────────
        String gameKey =
            pkg.contains("mobile.legends") || pkg.contains("mobilelegends")
                ? CompetitiveCfgProfile.GAME_MLBB :
            pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile")
                ? CompetitiveCfgProfile.GAME_PUBGM :
            pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("warzone")
                ? CompetitiveCfgProfile.GAME_CODM :
            pkg.contains("freefire") || pkg.contains("dts.freefire")
                ? CompetitiveCfgProfile.GAME_FREEFIRE :
            pkg.contains("genshin") || pkg.contains("mihoyo") || pkg.contains("cognosphere")
                || pkg.contains("hoyoverse") || pkg.contains("hkrpg") || pkg.contains("nap")
                // Wuthering Waves shares the Genshin/HoYo patcher path
                || pkg.contains("wutheringwaves") || pkg.contains("kurogame") || pkg.contains("kj")
                ? CompetitiveCfgProfile.GAME_GENSHIN :
            pkg.contains("wildrift") || pkg.contains("riotgames.league")
                ? CompetitiveCfgProfile.GAME_WILDRIFT :
            pkg.contains("sgame") || pkg.contains("levelinfinite") || pkg.contains("arenaofvalor")
                || pkg.contains("kgtw") || pkg.contains("kgvn")
                ? CompetitiveCfgProfile.GAME_HOK :
            pkg.contains("bloodstrike") || pkg.contains("newspike")
                ? CompetitiveCfgProfile.GAME_BLOODSTRIKE :
            pkg.contains("standoff2") || pkg.contains("axlebolt")
                ? CompetitiveCfgProfile.GAME_STANDOFF2 :
            pkg.contains("carx") || pkg.contains("glofta9hm") || pkg.contains("asphalt") || pkg.contains("r3_row")
                ? CompetitiveCfgProfile.GAME_CARX :
            pkg.contains("uamo") || pkg.contains("arenabreakout") || pkg.contains("deltaforce")
                ? CompetitiveCfgProfile.GAME_ARENABREAKOUT :
            pkg.contains("supercell") || pkg.contains("brawlstars")
                || pkg.contains("clashroyale") || pkg.contains("clashofclans")
                ? CompetitiveCfgProfile.GAME_SUPERCELL :
            pkg.contains("roblox")
                ? CompetitiveCfgProfile.GAME_ROBLOX :
            pkg.contains("projectc") || pkg.contains("valorant")
                ? CompetitiveCfgProfile.GAME_VALORANT :
            pkg.contains("farlight") || pkg.contains("solarland")
                ? CompetitiveCfgProfile.GAME_FARLIGHT :
            CompetitiveCfgProfile.GAME_ALL;

        CompetitiveCfgProfile currentCfg = CfgProfileManager.loadProfile(context, gameKey);

        // ── FPS options — prepend the UltraExtreme 144 SuperSmooth sentinel ──
        int[] baseFpsValues    = FpsUnlockTier.getAllFpsValues();
        String[] baseFpsLabels = FpsUnlockTier.getAllLabels();

        // Build combined arrays: [UltraExtreme sentinel] + [normal tiers]
        int[] fpsValues = new int[baseFpsValues.length + 1];
        String[] fpsOptions = new String[baseFpsLabels.length + 1];
        fpsValues[0]  = FPS_ULTRA_EXTREME_SENTINEL;
        fpsOptions[0] = "⚡ 144fps UltraExtreme SuperSmooth (MAX GRAPHICS + MAX FPS)";
        System.arraycopy(baseFpsValues, 0, fpsValues, 1, baseFpsValues.length);
        System.arraycopy(baseFpsLabels, 0, fpsOptions, 1, baseFpsLabels.length);

        // Default selection: match saved profile FPS (offset by 1); if not found, default to UltraExtreme
        int selectedIdx = 0;
        for (int i = 1; i < fpsValues.length; i++) {
            if (fpsValues[i] == currentCfg.getTargetFps()) { selectedIdx = i; break; }
        }
        final int[] chosenFps  = {fpsValues[selectedIdx]};
        final boolean[] superTouch = {currentCfg.isSuperFastTouchEnabled()};
        final boolean[] forceHz    = {currentCfg.isForceWriteSystemHz()};

        String[] multiOptions = {
                "⚡ Super Fast Touch 185Hz/165Hz (HighFreqTouch / TouchBoostHz 1000Hz)",
                "📺 Force System Hz via Shizuku (SurfaceFlinger + Game Mode + setprop)"
        };
        boolean[] initialChecked = {superTouch[0], forceHz[0]};

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("⚙️ " + game.getLabel() + " — Competitive CFG Config")
                .setMessage("Package: " + pkg + "\nSelect target FPS and touch options to force-write into game config files via Shizuku (temporary root):")
                .setSingleChoiceItems(fpsOptions, selectedIdx, (dialog, which) -> chosenFps[0] = fpsValues[which])
                .setMultiChoiceItems(multiOptions, initialChecked, (dialog, which, isChecked) -> {
                    if (which == 0) superTouch[0] = isChecked;
                    if (which == 1) forceHz[0] = isChecked;
                })
                .setPositiveButton("⚡ FORCE WRITE & APPLY VIA SHIZUKU", (dialog, which) -> {
                    final int selectedFps   = chosenFps[0];
                    final boolean isUltraEx = (selectedFps == FPS_ULTRA_EXTREME_SENTINEL || selectedFps >= 144);
                    // Resolve real FPS for profile saving (UltraExtreme → 144)
                    final int realFps       = (selectedFps == FPS_ULTRA_EXTREME_SENTINEL) ? 144 : selectedFps;
                    CompetitiveCfgProfile profile = new CompetitiveCfgProfile(gameKey, realFps, superTouch[0], forceHz[0]);

                    Toast.makeText(context,
                        isUltraEx
                            ? "⚡ Applying 144fps UltraExtreme SuperSmooth to " + game.getLabel() + " via Shizuku..."
                            : "⚡ Forcing " + realFps + " FPS CFG into " + game.getLabel() + " via Shizuku...",
                        Toast.LENGTH_SHORT).show();

                        // ── Step 1: Force-stop game ONLY if it is currently running,
                        // so cold-start picks up new configs without killing an already-exited game ──
                        try {
                            String pidCheck = ShizukuExecutor.executeShizukuCommand("pidof " + pkg + " 2>/dev/null");
                            boolean gameCurrentlyRunning = pidCheck != null && !pidCheck.trim().isEmpty()
                                    && !pidCheck.startsWith("ERROR");
                            if (gameCurrentlyRunning) {
                                ShizukuExecutor.executeShizukuCommand("am force-stop " + pkg + " 2>/dev/null");
                                // Give the process time to fully die before config injection
                                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                            }
                        } catch (Throwable ignored) {}


                        // ── Step 2: Apply config patchers ──
                        int patchedCount = CfgProfileManager.applyProfile(context, gameKey, profile);
                        if (isUltraEx) {
                            // Full UltraExtreme 144fps SuperSmooth path: graphics + FPS keys
                            GameConfigPatcher.applyUltraExtreme144Patch(context, pkg);
                        } else {
                            // Standard FPS-only path
                            GameConfigPatcher.applyGameFpsPatch(context, pkg, realFps);
                        }
                        GameProfileAutoConfigurator.autoConfigGamePackage(context, pkg, realFps);

                        // ── Step 3: Game Driver, ANGLE, Vulkan opt-in ──
                        ShizukuExecutor.executeShizukuCommands(
                            "settings put global game_driver_opt_in_apps " + pkg + " 2>/dev/null",
                            "settings put global updatable_driver_production_opt_in_apps " + pkg + " 2>/dev/null",
                            "settings put global angle_gl_driver_selection_pkgs " + pkg + " 2>/dev/null"
                        );

                        // ── Step 4: Android Game Mode API + per-app Hz override ──
                        ShizukuExecutor.executeShizukuCommands(
                            "cmd game mode performance " + pkg + " 2>/dev/null",
                            "cmd window set-app-refresh-rate " + pkg + " " + realFps + " 2>/dev/null",
                            "cmd game set --fps " + realFps + " " + pkg + " 2>/dev/null"
                        );

                        // ── Step 5: SurfaceFlinger direct Hz binder call (deepest level) ──
                        ShizukuExecutor.executeShizukuCommand(
                            "service call SurfaceFlinger 1035 i32 " + realFps + " 2>/dev/null");

                        // ── Step 6: Debug props for HWUI + render pipeline ──
                        ShizukuExecutor.executeShizukuCommands(
                            "setprop debug.sf.nobootanimation 1",
                            "setprop debug.hwui.render_dirty_regions false",
                            "setprop debug.sf.disable_backpressure 1"
                        );

                        // ── Step 7 (UltraExtreme only): SurfaceFlinger phase-offset props ──
                        if (isUltraEx) {
                            ShizukuExecutor.executeShizukuCommands(
                                "setprop debug.sf.use_phase_offsets_as_durations 1",
                                "setprop debug.sf.late.sf.duration 10500000",
                                "setprop debug.sf.late.app.duration 20500000",
                                "setprop debug.sf.hw 0",
                                "setprop debug.egl.hw 0",
                                "setprop persist.sys.ui.hw 1"
                            );
                        }

                        AppExecutors.getInstance().postToMainThread(() -> {
                            int pos = holder.getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION && pos < games.size()) {
                                holder.tvProfile.setText(GameProfilePreferences.getSummary(context, pkg));
                            }
                            String resultMsg = isUltraEx
                                ? "✅ 144fps UltraExtreme SuperSmooth applied to " + game.getLabel()
                                    + " (" + patchedCount + " files patched)! Game driver + SurfaceFlinger Hz locked via Shizuku!"
                                : "✅ FORCED " + realFps + " FPS to " + game.getLabel()
                                    + " (" + patchedCount + " files patched)! Game driver + SurfaceFlinger Hz locked via Shizuku!";
                            Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show();
                        });
                    })
                .setNegativeButton("Cancel", null);

        if (ConfigBackupManager.hasBackups(context, pkg)) {
            builder.setNeutralButton("♻️ Restore originals", (dialog, which) -> confirmRestore(game));
        }
        builder.show();
    }

    private void confirmRestore(GameAppInfo game) {
        int count = ConfigBackupManager.getBackupCount(context, game.getPackageName());
        new AlertDialog.Builder(context)
                .setTitle("♻️ Restore original config files")
                .setMessage("Restore " + count + " original config file(s) for " + game.getLabel() + "?\n\nOriginal file backups will be restored and removed. Patches can be re-applied afterwards.")
                .setPositiveButton("Restore", (dialog, which) -> {
                    Toast.makeText(context, "♻️ Restoring original files for " + game.getLabel() + "...", Toast.LENGTH_SHORT).show();
                    AppExecutors.getInstance().executeCommand(() -> {
                        int restored = ConfigBackupManager.restorePackage(context, game.getPackageName());
                        AppExecutors.getInstance().postToMainThread(() -> {
                            String msg = restored > 0
                                    ? "✅ Restored " + restored + " original config file(s) for " + game.getLabel()
                                    : "⚠️ Nothing to restore for " + game.getLabel();
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutCardBg;
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvPkg;
        TextView tvBadge;
        TextView tvProfile;
        Button btnLaunch;
        Button btnConfig;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutCardBg = itemView.findViewById(R.id.layout_card_bg);
            ivIcon = itemView.findViewById(R.id.iv_game_icon);
            tvTitle = itemView.findViewById(R.id.tv_game_title);
            tvPkg = itemView.findViewById(R.id.tv_game_pkg);
            tvBadge = itemView.findViewById(R.id.tv_game_badge);
            tvProfile = itemView.findViewById(R.id.tv_game_profile);
            btnLaunch = itemView.findViewById(R.id.btn_launch_game);
            btnConfig = itemView.findViewById(R.id.btn_config_game);
        }
    }
}
