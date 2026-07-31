package com.gamebooster.app.ui.layout;

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
import com.gamebooster.app.functions.TweakItem;
import com.gamebooster.app.functions.TweakManagerRepository;
import com.gamebooster.app.root.CommandExecutor;
import com.gamebooster.app.root.EngineMode;
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
        boolean hasShizuku = isShizukuAlive && (CommandExecutor.getActiveEngineMode() == EngineMode.SHIZUKU);
        boolean isLocked = needsShizuku && !hasShizuku;

        if (needsShizuku) {
            holder.tvBadge.setText(isShizukuAlive ? "[SHIZUKU/ADB]" : "[DISCONNECTED]");
            holder.tvBadge.setTextColor(isShizukuAlive ? Color.parseColor("#00FF66") : Color.parseColor("#FF0055"));
        } else {
            holder.tvBadge.setText("[SYSTEM SETTINGS]");
            holder.tvBadge.setTextColor(Color.parseColor("#00F0FF"));
        }

        if (isLocked) {
            holder.ivLock.setVisibility(View.VISIBLE);
            holder.switchToggle.setAlpha(0.4f);
            holder.switchToggle.setEnabled(false);
        } else {
            holder.ivLock.setVisibility(View.GONE);
            holder.switchToggle.setAlpha(1.0f);
            holder.switchToggle.setEnabled(true);
        }

        holder.switchToggle.setOnCheckedChangeListener(null);
        holder.switchToggle.setChecked(targetItem.isApplied());

        holder.switchToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (holder.isUpdatingProgrammatically) return;

                if (isLocked) {
                    holder.isUpdatingProgrammatically = true;
                    holder.switchToggle.setChecked(!isChecked);
                    holder.isUpdatingProgrammatically = false;
                    
                    ShizukuManager.showShizukuPermissionDialog(context, targetItem.getTitle());
                    return;
                }

                // Disable switch while processing in background thread
                holder.switchToggle.setEnabled(false);

                // Offload shell execution to AppExecutors.commandIO
                AppExecutors.getInstance().executeCommand(() -> {
                    boolean success = isChecked ? 
                            TweakManagerRepository.applyTweak(targetItem) : 
                            TweakManagerRepository.revertTweak(targetItem);

                    // Post callback back to main thread with recycling safety checks
                    AppExecutors.getInstance().postToMainThread(() -> {
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION && pos < tweaks.size() && tweaks.get(pos).equals(targetItem)) {
                            holder.switchToggle.setEnabled(true);
                            if (success) {
                                targetItem.setApplied(isChecked);
                                Toast.makeText(context, targetItem.getTitle() + (isChecked ? " APPLIED" : " REVERTED"), Toast.LENGTH_SHORT).show();
                            } else {
                                holder.isUpdatingProgrammatically = true;
                                holder.switchToggle.setChecked(!isChecked);
                                holder.isUpdatingProgrammatically = false;
                                Toast.makeText(context, "Failed to update " + targetItem.getTitle(), Toast.LENGTH_SHORT).show();
                            }
                        }
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
