package com.gamebooster.app.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClearGamesAdapter extends RecyclerView.Adapter<ClearGamesAdapter.ClearGameViewHolder> {

    public interface OnGameListModifiedListener {
        void onGameRemoved(GameAppInfo game, int remainingCount);
    }

    private final Context context;
    private final List<GameAppInfo> originalList = new ArrayList<>();
    private final List<GameAppInfo> filteredList = new ArrayList<>();
    private final Set<String> customPackages;
    private final OnGameListModifiedListener modifiedListener;
    private String currentQuery = "";

    public ClearGamesAdapter(Context context, List<GameAppInfo> games, OnGameListModifiedListener listener) {
        this.context = context;
        this.customPackages = GameLauncherHelper.getCustomPackages(context);
        this.modifiedListener = listener;
        if (games != null) {
            this.originalList.addAll(games);
            this.filteredList.addAll(games);
        }
    }

    public void updateList(List<GameAppInfo> games) {
        this.originalList.clear();
        this.filteredList.clear();
        this.customPackages.clear();
        this.customPackages.addAll(GameLauncherHelper.getCustomPackages(context));
        if (games != null) {
            this.originalList.addAll(games);
        }
        applyFilter();
    }

    public void filter(String query) {
        this.currentQuery = (query != null) ? query.trim().toLowerCase() : "";
        applyFilter();
    }

    private void applyFilter() {
        filteredList.clear();
        if (currentQuery.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            for (GameAppInfo g : originalList) {
                if (g == null) continue;
                String label = (g.getLabel() != null) ? g.getLabel().toLowerCase() : "";
                String pkg = (g.getPackageName() != null) ? g.getPackageName().toLowerCase() : "";
                if (label.contains(currentQuery) || pkg.contains(currentQuery)) {
                    filteredList.add(g);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClearGameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_clear_game_card, parent, false);
        return new ClearGameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClearGameViewHolder holder, int position) {
        GameAppInfo game = filteredList.get(position);
        holder.bind(game);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public int getTotalCount() {
        return originalList.size();
    }

    class ClearGameViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivIcon;
        private final TextView tvTitle;
        private final TextView tvPkg;
        private final TextView tvBadge;
        private final Button btnRemove;

        public ClearGameViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_clear_game_icon);
            tvTitle = itemView.findViewById(R.id.tv_clear_game_title);
            tvPkg = itemView.findViewById(R.id.tv_clear_game_pkg);
            tvBadge = itemView.findViewById(R.id.tv_clear_game_badge);
            btnRemove = itemView.findViewById(R.id.btn_clear_game_remove);
        }

        public void bind(GameAppInfo game) {
            if (game == null) return;

            tvTitle.setText(game.getLabel() != null ? game.getLabel() : game.getPackageName());
            tvPkg.setText(game.getPackageName());

            if (game.getIcon() != null) {
                ivIcon.setImageDrawable(game.getIcon());
            } else {
                ivIcon.setImageResource(R.drawable.badge_neon_cyan);
            }

            boolean isCustom = customPackages.contains(game.getPackageName());
            if (isCustom) {
                tvBadge.setText("CUSTOM");
                tvBadge.setTextColor(Color.parseColor("#00F0FF"));
                tvBadge.setBackgroundResource(R.drawable.badge_neon_cyan);
            } else {
                tvBadge.setText(game.getGameType() != null ? game.getGameType() : "DETECTED");
                tvBadge.setTextColor(Color.parseColor("#FFCC00"));
                tvBadge.setBackgroundResource(R.drawable.badge_neon_green);
            }

            btnRemove.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || pos >= filteredList.size()) return;

                GameAppInfo removedGame = filteredList.get(pos);
                String pkgToRemove = removedGame.getPackageName();
                String labelToRemove = removedGame.getLabel();

                // 1. Remove from persistent storage (both custom and excluded)
                GameLauncherHelper.removeGameFromHome(context, pkgToRemove);

                // 2. Remove from lists
                originalList.remove(removedGame);
                filteredList.remove(pos);
                notifyItemRemoved(pos);

                Toast.makeText(context, "🗑️ Removed " + (labelToRemove != null ? labelToRemove : pkgToRemove) + " from Home", Toast.LENGTH_SHORT).show();

                if (modifiedListener != null) {
                    modifiedListener.onGameRemoved(removedGame, originalList.size());
                }
            });
        }
    }
}
