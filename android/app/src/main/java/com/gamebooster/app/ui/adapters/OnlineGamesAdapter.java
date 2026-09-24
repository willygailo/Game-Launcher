package com.gamebooster.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gamebooster.app.R;
import com.gamebooster.app.api.OnlineGameSearchResult;
import com.gamebooster.app.ui.activities.WebBrowserActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OnlineGamesAdapter extends RecyclerView.Adapter<OnlineGamesAdapter.ViewHolder> {

    private final Context context;
    private final List<OnlineGameSearchResult> items = new ArrayList<>();

    public OnlineGamesAdapter(Context context) {
        this.context = context;
    }

    public void updateList(List<OnlineGameSearchResult> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_online_game_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnlineGameSearchResult item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvCategory.setText(item.getCategory().toUpperCase(Locale.ROOT));
        holder.tvDescription.setText(item.getDescription());
        holder.tvRating.setText(String.format(Locale.US, "⭐ %.1f • %s", item.getRating(), item.isWebGame() ? "Web Game" : "Online"));
        holder.btnLaunch.setText(item.isWebGame() ? "PLAY" : "VIEW");

        if (item.getCoverUrl() != null && !item.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getCoverUrl())
                    .placeholder(android.R.drawable.ic_menu_compass)
                    .error(android.R.drawable.ic_menu_compass)
                    .into(holder.ivIcon);
        } else {
            holder.ivIcon.setImageResource(item.isWebGame() ? android.R.drawable.ic_media_play : android.R.drawable.ic_menu_compass);
        }

        View.OnClickListener launchClick = v -> {
            String url = item.getWebUrl();
            if (url == null || url.trim().isEmpty()) {
                url = "https://html.duckduckgo.com/html/?q=" + android.net.Uri.encode(item.getTitle() + " online game");
            }
            WebBrowserActivity.openUrl(context, url, item.getTitle());
        };

        holder.itemView.setOnClickListener(launchClick);
        holder.btnLaunch.setOnClickListener(launchClick);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTitle;
        final TextView tvCategory;
        final TextView tvDescription;
        final TextView tvRating;
        final Button btnLaunch;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_online_game_icon);
            tvTitle = itemView.findViewById(R.id.tv_online_game_title);
            tvCategory = itemView.findViewById(R.id.tv_online_game_category);
            tvDescription = itemView.findViewById(R.id.tv_online_game_desc);
            tvRating = itemView.findViewById(R.id.tv_online_game_rating);
            btnLaunch = itemView.findViewById(R.id.btn_online_game_launch);
        }
    }
}
