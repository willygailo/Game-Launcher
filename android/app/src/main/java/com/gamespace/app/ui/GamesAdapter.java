package com.gamespace.app.ui;

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

import com.gamespace.app.R;
import com.gamespace.app.core.GameAppInfo;

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
        if (game.getIcon() != null) {
            holder.ivIcon.setImageDrawable(game.getIcon());
        }

        holder.btnLaunch.setOnClickListener(v -> {
            if (game.getLaunchIntent() != null) {
                try {
                    Toast.makeText(context, "Boosting & Launching " + game.getLabel() + "...", Toast.LENGTH_SHORT).show();
                    context.startActivity(game.getLaunchIntent());
                } catch (Exception e) {
                    Toast.makeText(context, "Unable to launch " + game.getLabel(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Cannot launch " + game.getLabel() + " (No launcher intent)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvPkg;
        Button btnLaunch;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_game_icon);
            tvTitle = itemView.findViewById(R.id.tv_game_title);
            tvPkg = itemView.findViewById(R.id.tv_game_pkg);
            btnLaunch = itemView.findViewById(R.id.btn_launch_game);
        }
    }
}
