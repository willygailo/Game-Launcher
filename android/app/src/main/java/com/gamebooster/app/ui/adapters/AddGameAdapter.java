package com.gamebooster.app.ui.adapters;

import android.content.Context;
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
import com.gamebooster.app.games.GameLauncherHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddGameAdapter extends RecyclerView.Adapter<AddGameAdapter.AddGameViewHolder> {

    public static class AppPickerItem {
        public final String appName;
        public final String packageName;
        public final Drawable icon;
        public boolean isAdded;

        public AppPickerItem(String appName, String packageName, Drawable icon, boolean isAdded) {
            this.appName = appName;
            this.packageName = packageName;
            this.icon = icon;
            this.isAdded = isAdded;
        }
    }

    private final Context context;
    private final List<AppPickerItem> originalList = new ArrayList<>();
    private final List<AppPickerItem> filteredList = new ArrayList<>();
    private final Runnable onChangeCallback;

    public AddGameAdapter(Context context, List<AppPickerItem> items, Runnable onChangeCallback) {
        this.context = context;
        if (items != null) {
            this.originalList.addAll(items);
            this.filteredList.addAll(items);
        }
        this.onChangeCallback = onChangeCallback;
    }

    public void updateList(List<AppPickerItem> items) {
        originalList.clear();
        filteredList.clear();
        if (items != null) {
            originalList.addAll(items);
            filteredList.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String lower = query.toLowerCase().trim();
            for (AppPickerItem item : originalList) {
                if (item.appName.toLowerCase().contains(lower) || item.packageName.toLowerCase().contains(lower)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddGameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_add_game_picker, parent, false);
        return new AddGameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddGameViewHolder holder, int position) {
        AppPickerItem item = filteredList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    class AddGameViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivIcon;
        private final TextView tvTitle;
        private final TextView tvPkg;
        private final Button btnToggle;

        public AddGameViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_pick_game_icon);
            tvTitle = itemView.findViewById(R.id.tv_pick_game_title);
            tvPkg = itemView.findViewById(R.id.tv_pick_game_pkg);
            btnToggle = itemView.findViewById(R.id.btn_pick_game_toggle);
        }

        public void bind(AppPickerItem item) {
            tvTitle.setText(item.appName);
            tvPkg.setText(item.packageName);

            if (item.icon != null) {
                ivIcon.setImageDrawable(item.icon);
            } else {
                ivIcon.setImageResource(R.drawable.badge_neon_cyan);
            }

            updateButtonState(item);

            btnToggle.setOnClickListener(v -> {
                item.isAdded = !item.isAdded;
                if (item.isAdded) {
                    GameLauncherHelper.addCustomPackage(context, item.packageName);
                    Toast.makeText(context, "Added " + item.appName + " to HOME!", Toast.LENGTH_SHORT).show();
                } else {
                    GameLauncherHelper.removeCustomPackage(context, item.packageName);
                    Toast.makeText(context, "Removed " + item.appName + " from HOME", Toast.LENGTH_SHORT).show();
                }
                updateButtonState(item);
                if (onChangeCallback != null) onChangeCallback.run();
            });
        }

        private void updateButtonState(AppPickerItem item) {
            if (item.isAdded) {
                btnToggle.setText("✓ ADDED");
                btnToggle.setBackgroundResource(R.drawable.btn_cyber_dark);
                btnToggle.setTextColor(0xFF00FF66);
            } else {
                btnToggle.setText("➕ ADD");
                btnToggle.setBackgroundResource(R.drawable.btn_cyber_cyan);
                btnToggle.setTextColor(0xFF080B11);
            }
        }
    }
}
