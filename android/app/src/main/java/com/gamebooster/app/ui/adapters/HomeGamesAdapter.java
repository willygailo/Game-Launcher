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

        holder.btnLaunch.setOnClickListener(v -> com.gamebooster.app.gamemanager.GameManagerLauncher.launchGame(context, game));
        holder.btnConfig.setOnClickListener(v -> showProfilePicker(holder, game));
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    private void showProfilePicker(GameViewHolder holder, GameAppInfo game) {
        String pkg = game.getPackageName().toLowerCase();
        String gameKey = pkg.contains("mobile.legends") || pkg.contains("mobilelegends") ? CompetitiveCfgProfile.GAME_MLBB :
                         pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile") ? CompetitiveCfgProfile.GAME_PUBGM :
                         pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("warzone") ? CompetitiveCfgProfile.GAME_CODM :
                         pkg.contains("freefire") || pkg.contains("dts.freefire") ? CompetitiveCfgProfile.GAME_FREEFIRE :
                         pkg.contains("genshin") || pkg.contains("mihoyo") || pkg.contains("cognosphere") || pkg.contains("hoyoverse") || pkg.contains("hkrpg") || pkg.contains("nap") ? CompetitiveCfgProfile.GAME_GENSHIN :
                         pkg.contains("wildrift") || pkg.contains("riotgames.league") ? CompetitiveCfgProfile.GAME_WILDRIFT :
                         pkg.contains("sgame") || pkg.contains("levelinfinite") || pkg.contains("arenaofvalor") || pkg.contains("kgtw") || pkg.contains("kgvn") ? CompetitiveCfgProfile.GAME_HOK :
                         pkg.contains("bloodstrike") || pkg.contains("newspike") ? CompetitiveCfgProfile.GAME_BLOODSTRIKE :
                         pkg.contains("standoff2") || pkg.contains("axlebolt") ? CompetitiveCfgProfile.GAME_STANDOFF2 :
                         pkg.contains("carx") || pkg.contains("glofta9hm") || pkg.contains("asphalt") || pkg.contains("r3_row") ? CompetitiveCfgProfile.GAME_CARX :
                         pkg.contains("uamo") || pkg.contains("arenabreakout") || pkg.contains("deltaforce") ? CompetitiveCfgProfile.GAME_ARENABREAKOUT :
                         pkg.contains("supercell") || pkg.contains("brawlstars") || pkg.contains("clashroyale") || pkg.contains("clashofclans") ? CompetitiveCfgProfile.GAME_SUPERCELL :
                         pkg.contains("roblox") ? CompetitiveCfgProfile.GAME_ROBLOX :
                         pkg.contains("projectc") || pkg.contains("valorant") ? CompetitiveCfgProfile.GAME_VALORANT :
                         pkg.contains("farlight") || pkg.contains("solarland") ? CompetitiveCfgProfile.GAME_FARLIGHT : CompetitiveCfgProfile.GAME_ALL;

        CompetitiveCfgProfile currentCfg = CfgProfileManager.loadProfile(context, gameKey);

        int[] fpsValues = FpsUnlockTier.getAllFpsValues();
        String[] fpsOptions = FpsUnlockTier.getAllLabels();

        int selectedIdx = 0;
        for (int i = 0; i < fpsValues.length; i++) {
            if (fpsValues[i] == currentCfg.getTargetFps()) selectedIdx = i;
        }
        final int[] chosenFps = {currentCfg.getTargetFps()};
        final boolean[] superTouch = {currentCfg.isSuperFastTouchEnabled()};
        final boolean[] forceHz = {currentCfg.isForceWriteSystemHz()};

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
                    CompetitiveCfgProfile profile = new CompetitiveCfgProfile(gameKey, chosenFps[0], superTouch[0], forceHz[0]);

                    Toast.makeText(context, "⚡ Forcing " + chosenFps[0] + " FPS CFG into " + game.getLabel() + " game files via Shizuku...", Toast.LENGTH_SHORT).show();

                    AppExecutors.getInstance().executeCommand(() -> {
                        int patchedCount = CfgProfileManager.applyProfile(context, gameKey, profile);
                        GameConfigPatcher.applyGameFpsPatch(context, pkg, chosenFps[0]);
                        GameProfileAutoConfigurator.autoConfigGamePackage(context, pkg, chosenFps[0]);

                        ShizukuExecutor.executeShizukuCommand("settings put global game_driver_opt_in_apps " + pkg);
                        ShizukuExecutor.executeShizukuCommand("settings put global updatable_driver_production_opt_in_apps " + pkg);

                        AppExecutors.getInstance().postToMainThread(() -> {
                            int pos = holder.getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION && pos < games.size()) {
                                holder.tvProfile.setText(GameProfilePreferences.getSummary(context, pkg));
                            }
                            Toast.makeText(context, "✅ FORCED " + chosenFps[0] + " FPS CFG TO " + game.getLabel() + " (" + patchedCount + " files updated via Shizuku)!", Toast.LENGTH_LONG).show();
                        });
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
