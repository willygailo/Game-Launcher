package com.gamebooster.app.ui.adapters;
import com.gamebooster.app.config.*;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameLauncherHelper;
import com.gamebooster.app.config.GameProfilePreferences;

import java.util.List;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.GameViewHolder> {

    private final Context context;
    private final List<GameAppInfo> games;

    public GamesAdapter(Context context, List<GameAppInfo> games) {
        this.context = context;
        this.games = games;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game_card, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameAppInfo game = games.get(position);
        holder.tvTitle.setText(game.getLabel());
        holder.tvPkg.setText(game.getPackageName());
        holder.tvProfile.setText(GameProfilePreferences.getSummary(context, game.getPackageName()));
        if (game.getIcon() != null) holder.ivIcon.setImageDrawable(game.getIcon());

        String note = com.gamebooster.app.config.GameConfigPatchVerifier.getPatchCompatibilityNote(game.getPackageName());
        if (note != null) {
            holder.tvNote.setText("⚠ " + note);
            holder.tvNote.setVisibility(View.VISIBLE);
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        holder.btnLaunch.setOnClickListener(v -> GameLauncherHelper.launchGameWithAutoBoost(context, game));
        holder.btnConfig.setOnClickListener(v -> showProfilePicker(holder, game));
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    private void showProfilePicker(GameViewHolder holder, GameAppInfo game) {
        GameProfilePreferences.Profile[] profiles = GameProfilePreferences.Profile.values();
        String[] labels = new String[profiles.length];
        GameProfilePreferences.Profile current = GameProfilePreferences.getProfile(context, game.getPackageName());
        int selected = 0;
        for (int i = 0; i < profiles.length; i++) {
            int fps = GameProfilePreferences.getTargetHz(context, profiles[i]);
            labels[i] = profiles[i].label + " — Force " + fps + " FPS/Hz into game files";
            if (profiles[i] == current) selected = i;
        }

        final int[] chosenIdx = {selected};

        new AlertDialog.Builder(context)
                .setTitle("⚙️ " + game.getLabel() + " FPS Config")
                .setMessage("Package: " + game.getPackageName() + "\nChoose target rate to force inject into internal game config files:")
                .setSingleChoiceItems(labels, selected, (dialog, which) -> chosenIdx[0] = which)
                .setPositiveButton("⚡ FORCE WRITE FPS TO GAME FILES", (dialog, which) -> {
                    GameProfilePreferences.Profile chosen = profiles[chosenIdx[0]];
                    int targetFps = GameProfilePreferences.getTargetHz(context, chosen);
                    GameProfilePreferences.setProfile(context, game.getPackageName(), chosen);
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && pos < games.size()) {
                        holder.tvProfile.setText(GameProfilePreferences.getSummary(context, game.getPackageName()));
                    }

                    Toast.makeText(context, "⚡ Forcing " + targetFps + " FPS config into " + game.getLabel() + " game files...", Toast.LENGTH_SHORT).show();

                    com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                        // 1. Force patch internal & external game INI config files
                        GameConfigPatcher.PatchResult result = GameConfigPatcher.applyGameFpsPatch(context, game.getPackageName(), targetFps);

                        // 2. Configure Game Mode API & refresh rate overrides
                        GameProfileAutoConfigurator.autoConfigGamePackage(context, game.getPackageName(), targetFps);

                        // 3. Opt game package into System Game Driver via Shizuku
                        com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("settings put global game_driver_opt_in_apps " + game.getPackageName());
                        com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("settings put global updatable_driver_production_opt_in_apps " + game.getPackageName());

                        com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() -> {
                            Toast.makeText(context, "✅ FORCED " + targetFps + " FPS CONFIG TO " + game.getLabel() + " GAME FILES!\n" + result.message, Toast.LENGTH_LONG).show();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvPkg;
        TextView tvProfile;
        TextView tvNote;
        Button btnLaunch;
        Button btnConfig;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_game_icon);
            tvTitle = itemView.findViewById(R.id.tv_game_title);
            tvPkg = itemView.findViewById(R.id.tv_game_pkg);
            tvProfile = itemView.findViewById(R.id.tv_game_profile);
            tvNote = itemView.findViewById(R.id.tv_game_note);
            btnLaunch = itemView.findViewById(R.id.btn_launch_game);
            btnConfig = itemView.findViewById(R.id.btn_config_game);
        }
    }
}