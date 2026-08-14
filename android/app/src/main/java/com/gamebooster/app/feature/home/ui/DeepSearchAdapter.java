package com.gamebooster.app.feature.home.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.feature.games.search.DiscoveredGameItem;

import java.util.ArrayList;
import java.util.List;

public class DeepSearchAdapter extends RecyclerView.Adapter<DeepSearchAdapter.ViewHolder> {

    public interface OnGameAddListener {
        void onGameAdd(DiscoveredGameItem item);
    }

    private final Context context;
    private final List<DiscoveredGameItem> items = new ArrayList<>();
    private final OnGameAddListener listener;

    public DeepSearchAdapter(Context context, OnGameAddListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<DiscoveredGameItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_deep_search_game, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiscoveredGameItem item = items.get(position);
        holder.tvLabel.setText(item.getLabel());
        holder.tvPkg.setText(item.getPackageName());

        if (item.getIcon() != null) {
            holder.ivIcon.setImageDrawable(item.getIcon());
        }

        holder.tvEngineBadge.setText(item.getEngineType().label);
        try {
            holder.tvEngineBadge.setTextColor(Color.parseColor(item.getEngineType().colorHex));
        } catch (Throwable ignored) {}

        holder.tvSourceBadge.setText(item.getDiscoverySource());

        if (item.isAddedToLibrary()) {
            holder.btnAdd.setText("✓ ADDED");
            holder.btnAdd.setEnabled(false);
        } else {
            holder.btnAdd.setText("➕ ADD");
            holder.btnAdd.setEnabled(true);
            holder.btnAdd.setOnClickListener(v -> {
                item.setAddedToLibrary(true);
                notifyItemChanged(holder.getAdapterPosition());
                if (listener != null) listener.onGameAdd(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvLabel;
        TextView tvPkg;
        TextView tvEngineBadge;
        TextView tvSourceBadge;
        Button btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_discovered_game_icon);
            tvLabel = itemView.findViewById(R.id.tv_discovered_game_label);
            tvPkg = itemView.findViewById(R.id.tv_discovered_game_pkg);
            tvEngineBadge = itemView.findViewById(R.id.tv_discovered_engine_badge);
            tvSourceBadge = itemView.findViewById(R.id.tv_discovered_source_badge);
            btnAdd = itemView.findViewById(R.id.btn_add_to_library);
        }
    }
}
