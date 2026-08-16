package com.gamebooster.app.ui.screens;
import com.gamebooster.app.config.*;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.tweaks.TweakItem;
import com.gamebooster.app.tweaks.TweakManagerRepository;
import com.gamebooster.app.config.TweakPreferences;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.EngineMode;
import com.gamebooster.app.shizuku.ShizukuManager;

import java.util.List;

public class TweaksAdapter extends RecyclerView.Adapter<TweaksAdapter.TweakViewHolder> {

    private final Context context;
    private List<TweakItem> tweaks;
    private boolean isShizukuAlive = true;

    public TweaksAdapter(Context context, List<TweakItem> tweaks) {
        this.context = context;
        this.tweaks = tweaks;
    }

    public void updateList(List<TweakItem> newTweaks) {
        this.tweaks = newTweaks;
        notifyDataSetChanged();
    }

    public void setShizukuAlive(boolean alive) {
        this.isShizukuAlive = alive;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TweakViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweak_card, parent, false);
        return new TweakViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TweakViewHolder holder, int position) {
        final TweakItem targetItem = tweaks.get(position);
        holder.tvTitle.setText(targetItem.getTitle());
        holder.tvDescription.setText(targetItem.getDescription());

        boolean needsShizuku = targetItem.isRequiresShizuku();
        holder.tvBadge.setText(isShizukuAlive ? "[SHIZUKU/ADB]" : "[ADVANCED ENGINE]");
        holder.tvBadge.setTextColor(isShizukuAlive ? Color.parseColor("#00FF66") : Color.parseColor("#00F0FF"));
        holder.ivLock.setVisibility(View.GONE);
        holder.switchToggle.setAlpha(1.0f);
        holder.switchToggle.setEnabled(true);

        // Guard: prevent listener from firing during programmatic setChecked
        holder.isUpdatingProgrammatically = true;
        boolean isAppliedState = TweakPreferences.isTweakApplied(context, targetItem.getId());
        targetItem.setApplied(isAppliedState);
        holder.switchToggle.setOnCheckedChangeListener(null);
        holder.switchToggle.setChecked(isAppliedState);
        holder.isUpdatingProgrammatically = false;

        holder.switchToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (holder.isUpdatingProgrammatically) return;

                // If Shizuku is available but not yet granted, request permission
                if (!com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission() && com.gamebooster.app.shizuku.ShizukuExecutor.isShizukuAvailable()) {
                    try {
                        rikka.shizuku.Shizuku.requestPermission(1001);
                    } catch (Throwable ignored) {}
                }

                // 1. Immediately persist & animate state
                targetItem.setApplied(isChecked);
                TweakPreferences.saveTweakState(context, targetItem.getId(), isChecked);

                // 2. Offload execution to background AppExecutors
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        TweakManagerRepository.applyTweak(context, targetItem);
                    } else {
                        TweakManagerRepository.revertTweak(context, targetItem);
                    }

                    AppExecutors.getInstance().postToMainThread(() -> {
                        Toast.makeText(context, (isChecked ? "⚡ Activated: " : "Deactivated: ") + targetItem.getTitle(), Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return tweaks.size();
    }

    public static class TweakViewHolder extends RecyclerView.ViewHolder {
        TextView tvBadge;
        TextView tvTitle;
        TextView tvDescription;
        Switch switchToggle;
        ImageView ivLock;
        boolean isUpdatingProgrammatically = false;

        public TweakViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBadge = itemView.findViewById(R.id.tv_tweak_badge);
            tvTitle = itemView.findViewById(R.id.tv_tweak_title);
            tvDescription = itemView.findViewById(R.id.tv_tweak_description);
            switchToggle = itemView.findViewById(R.id.switch_tweak_toggle);
            ivLock = itemView.findViewById(R.id.iv_tweak_lock);
        }
    }
}
