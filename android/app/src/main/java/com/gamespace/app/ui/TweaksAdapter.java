package com.gamespace.app.ui;

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

import com.gamespace.app.R;
import com.gamespace.app.tweaks.TweakItem;
import com.gamespace.app.tweaks.TweakManagerRepository;
import com.gamespace.app.data.CommandExecutor;
import com.gamespace.app.utils.ShizukuExecutor;
import com.gamespace.app.utils.ShizukuUtils;

import java.util.List;

public class TweaksAdapter extends RecyclerView.Adapter<TweaksAdapter.TweakViewHolder> {

    private final Context context;
    private List<TweakItem> tweaks;

    public TweaksAdapter(Context context, List<TweakItem> tweaks) {
        this.context = context;
        this.tweaks = tweaks;
    }

    public void updateList(List<TweakItem> newTweaks) {
        this.tweaks = newTweaks;
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
        TweakItem tweak = tweaks.get(position);
        holder.tvTitle.setText(tweak.getTitle());
        holder.tvDescription.setText(tweak.getDescription());

        boolean needsShizuku = tweak.isRequiresShizuku();
        boolean hasShizuku = CommandExecutor.getActiveEngineMode() == com.gamespace.app.core.EngineMode.SHIZUKU;

        boolean isLocked = needsShizuku && !hasShizuku;

        if (needsShizuku) {
            holder.tvBadge.setText("[SHIZUKU/ADB]");
            holder.tvBadge.setTextColor(Color.parseColor("#00FF66"));
        } else {
            holder.tvBadge.setText("[SYSTEM SETTINGS]");
            holder.tvBadge.setTextColor(Color.parseColor("#00F0FF"));
        }

        if (isLocked) {
            holder.ivLock.setVisibility(View.VISIBLE);
            holder.switchToggle.setAlpha(0.5f);
        } else {
            holder.ivLock.setVisibility(View.GONE);
            holder.switchToggle.setAlpha(1.0f);
        }

        holder.switchToggle.setOnCheckedChangeListener(null);
        holder.switchToggle.setChecked(tweak.isApplied());

        holder.switchToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (holder.isUpdatingProgrammatically) return;

                if (isLocked) {
                    holder.isUpdatingProgrammatically = true;
                    holder.switchToggle.setChecked(!isChecked);
                    holder.isUpdatingProgrammatically = false;
                    
                    ShizukuUtils.showShizukuPermissionDialog(context, tweak.getTitle());
                    return;
                }

                boolean success;
                if (isChecked) {
                    success = TweakManagerRepository.applyTweak(tweak);
                    if (success) {
                        Toast.makeText(context, tweak.getTitle() + " APPLIED", Toast.LENGTH_SHORT).show();
                    } else {
                        holder.isUpdatingProgrammatically = true;
                        holder.switchToggle.setChecked(false);
                        holder.isUpdatingProgrammatically = false;
                        Toast.makeText(context, "Failed to apply " + tweak.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    success = TweakManagerRepository.revertTweak(tweak);
                    if (success) {
                        Toast.makeText(context, tweak.getTitle() + " REVERTED", Toast.LENGTH_SHORT).show();
                    } else {
                        holder.isUpdatingProgrammatically = true;
                        holder.switchToggle.setChecked(true);
                        holder.isUpdatingProgrammatically = false;
                        Toast.makeText(context, "Failed to revert " + tweak.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
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
