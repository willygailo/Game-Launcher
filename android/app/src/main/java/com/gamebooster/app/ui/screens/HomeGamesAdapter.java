package com.gamebooster.app.ui.screens;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.games.GameAppInfo;

import java.util.ArrayList;
import java.util.List;

public class HomeGamesAdapter extends RecyclerView.Adapter<HomeGamesAdapter.GameViewHolder> {

    public interface OnGameSelectedListener {
        void onGameSelected(GameAppInfo game, int position);
    }

    private final Context context;
    private final List<GameAppInfo> games = new ArrayList<>();
    private int selectedPosition = 0;
    private OnGameSelectedListener listener;

    public HomeGamesAdapter(Context context, List<GameAppInfo> initialGames) {
        this.context = context;
        if (initialGames != null) {
            this.games.addAll(initialGames);
        }
    }

    public void setOnGameSelectedListener(OnGameSelectedListener listener) {
        this.listener = listener;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        if (position >= 0 && position < games.size() && position != selectedPosition) {
            int previous = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onGameSelected(games.get(selectedPosition), selectedPosition);
            }
        }
    }

    public GameAppInfo getSelectedGame() {
        if (!games.isEmpty() && selectedPosition >= 0 && selectedPosition < games.size()) {
            return games.get(selectedPosition);
        }
        return null;
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

        if (selectedPosition >= games.size()) {
            selectedPosition = Math.max(0, games.size() - 1);
        }

        diffResult.dispatchUpdatesTo(this);

        if (!games.isEmpty() && listener != null) {
            listener.onGameSelected(games.get(selectedPosition), selectedPosition);
        }
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game_space_card, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameAppInfo game = games.get(position);
        holder.tvTitle.setText(game.getLabel());

        if (game.getIcon() != null) {
            holder.ivIcon.setImageDrawable(game.getIcon());
        }

        if (game.getGameType() != null) {
            holder.tvBadge.setText(game.getGameType());
        }

        if (game.getBadgeColor() != 0) {
            holder.tvBadge.setTextColor(game.getBadgeColor());
        }

        boolean isSelected = (position == selectedPosition);
        holder.cardContainer.setBackgroundResource(isSelected ? R.drawable.bg_game_card_selected : R.drawable.bg_game_card_normal);
        holder.viewIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> setSelectedPosition(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        FrameLayout cardContainer;
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvBadge;
        View viewIndicator;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_container);
            ivIcon = itemView.findViewById(R.id.iv_game_icon);
            tvTitle = itemView.findViewById(R.id.tv_game_title);
            tvBadge = itemView.findViewById(R.id.tv_game_badge);
            viewIndicator = itemView.findViewById(R.id.view_selected_indicator);
        }
    }
}
