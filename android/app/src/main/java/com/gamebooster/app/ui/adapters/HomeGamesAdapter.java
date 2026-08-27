package com.gamebooster.app.ui.adapters;

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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.ui.dialogs.PreLaunchGameDialog;

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

        View.OnClickListener launchListener = v -> PreLaunchGameDialog.show(context, game);
        holder.btnLaunch.setOnClickListener(launchListener);
        if (holder.layoutCardBg != null) {
            holder.layoutCardBg.setOnClickListener(launchListener);
        }
        holder.itemView.setOnClickListener(launchListener);
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutCardBg;
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvPkg;
        TextView tvBadge;
        TextView tvProfile;
        Button btnLaunch;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutCardBg = itemView.findViewById(R.id.layout_card_bg);
            ivIcon = itemView.findViewById(R.id.iv_game_icon);
            tvTitle = itemView.findViewById(R.id.tv_game_title);
            tvPkg = itemView.findViewById(R.id.tv_game_pkg);
            tvBadge = itemView.findViewById(R.id.tv_game_badge);
            tvProfile = itemView.findViewById(R.id.tv_game_profile);
            btnLaunch = itemView.findViewById(R.id.btn_launch_game);
        }
    }
}
