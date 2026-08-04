package com.gamebooster.app.ui.screens;

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
import com.gamebooster.app.config.GameConfigPatcher;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.config.GameProfilePreferences;
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
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return games.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return games.get(oldItemPosition).getPackageName()
                        .equalsIgnoreCase(newList.get(newItemPosition).getPackageName());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                GameAppInfo oldItem = games.get(oldItemPosition);
                GameAppInfo newItem = newList.get(newItemPosition);
                return oldItem.getLabel().equals(newItem.getLabel())
                        && oldItem.getPackageName().equals(newItem.getPackageName());
            }
        });

        games.clear();
        games.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
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
                    holder.tvProfile.setText(GameProfilePreferences.getSummary(context, game.getPackageName()));

                    Toast.makeText(context, "⚡ Forcing " + targetFps + " FPS config into " + game.getLabel() + " game files...", Toast.LENGTH_SHORT).show();

                    AppExecutors.getInstance().executeCommand(() -> {
                        GameConfigPatcher.PatchResult result = GameConfigPatcher.applyGameFpsPatch(game.getPackageName(), targetFps);
                        GameProfileAutoConfigurator.autoConfigGamePackage(context, game.getPackageName(), targetFps);
                        ShizukuExecutor.executeShizukuCommand("settings put global game_driver_opt_in_apps " + game.getPackageName());
                        ShizukuExecutor.executeShizukuCommand("settings put global updatable_driver_production_opt_in_apps " + game.getPackageName());

                        AppExecutors.getInstance().postToMainThread(() -> {
                            Toast.makeText(context, "✅ FORCED " + targetFps + " FPS CONFIG TO " + game.getLabel() + " GAME FILES!\n" + result.message, Toast.LENGTH_LONG).show();
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
