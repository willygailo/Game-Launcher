package com.gamebooster.app.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.config.TweakPreferences;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.ShellExecutor;
import com.gamebooster.app.shizuku.RishManager;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.tweaks.TweakCategory;
import com.gamebooster.app.tweaks.TweakItem;
import com.gamebooster.app.tweaks.TweakManagerRepository;

import java.util.ArrayList;
import java.util.List;

public class TweaksAdapter extends RecyclerView.Adapter<TweaksAdapter.TweakViewHolder> {

    public interface OnTweakStateChangeListener {
        void onTweakStateChanged(TweakItem item, boolean isApplied, int totalAppliedCount);
    }

    private final Context context;
    private final List<TweakItem> allMasterTweaks;
    private List<TweakItem> tweaks;
    private boolean isShizukuAlive = true;
    private String currentQuery = "";
    private TweakCategory currentCategory = TweakCategory.ALL;
    private OnTweakStateChangeListener stateChangeListener;

    public TweaksAdapter(Context context, List<TweakItem> tweaks) {
        this.context = context;
        this.allMasterTweaks = new ArrayList<>(tweaks != null ? tweaks : new ArrayList<>());
        this.tweaks = new ArrayList<>(this.allMasterTweaks);
    }

    public void setOnTweakStateChangeListener(OnTweakStateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    public void updateList(List<TweakItem> newTweaks) {
        this.allMasterTweaks.clear();
        if (newTweaks != null) {
            this.allMasterTweaks.addAll(newTweaks);
        }
        applyFilter();
    }

    public void filter(String query, TweakCategory category) {
        this.currentQuery = (query != null) ? query.trim().toLowerCase() : "";
        if (category != null) {
            this.currentCategory = category;
        }
        applyFilter();
    }

    public void setCategoryFilter(TweakCategory category) {
        this.currentCategory = (category != null) ? category : TweakCategory.ALL;
        applyFilter();
    }

    public void setSearchQuery(String query) {
        this.currentQuery = (query != null) ? query.trim().toLowerCase() : "";
        applyFilter();
    }

    private void applyFilter() {
        List<TweakItem> result = new ArrayList<>();
        for (TweakItem item : allMasterTweaks) {
            if (item == null) continue;

            // Category match
            if (currentCategory != TweakCategory.ALL && item.getCategory() != currentCategory) {
                continue;
            }

            // Search query match
            if (!currentQuery.isEmpty()) {
                String title = item.getTitle() != null ? item.getTitle().toLowerCase() : "";
                String desc = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
                String id = item.getId() != null ? item.getId().toLowerCase() : "";

                boolean matches = title.contains(currentQuery)
                        || desc.contains(currentQuery)
                        || id.contains(currentQuery);

                // Smart aliases
                if (!matches) {
                    if ((currentQuery.equals("ml") || currentQuery.equals("mlbb")) && (id.contains("mlbb") || title.contains("mobile legends") || desc.contains("mobile legends"))) {
                        matches = true;
                    } else if ((currentQuery.equals("pubg") || currentQuery.equals("bgmi")) && (id.contains("pubgm") || title.contains("pubg") || desc.contains("pubg"))) {
                        matches = true;
                    } else if ((currentQuery.equals("cod") || currentQuery.equals("codm")) && (id.contains("codm") || title.contains("cod") || desc.contains("call of duty"))) {
                        matches = true;
                    } else if (currentQuery.equals("ff") && (id.contains("freefire") || title.contains("free fire") || desc.contains("free fire"))) {
                        matches = true;
                    } else if ((currentQuery.equals("val") || currentQuery.equals("valo")) && (id.contains("valorant") || title.contains("valorant"))) {
                        matches = true;
                    } else if (currentQuery.equals("fps") && (title.contains("fps") || title.contains("185hz") || title.contains("frame rate") || desc.contains("fps"))) {
                        matches = true;
                    } else if (currentQuery.equals("vulkan") && (title.contains("vulkan") || desc.contains("vulkan"))) {
                        matches = true;
                    } else if (currentQuery.equals("touch") && (title.contains("touch") || desc.contains("touch") || desc.contains("latency"))) {
                        matches = true;
                    } else if (currentQuery.equals("gyro") && (title.contains("gyro") || desc.contains("gyro"))) {
                        matches = true;
                    }
                }

                if (!matches) continue;
            }

            result.add(item);
        }

        this.tweaks = result;
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

        boolean hasShizuku = ShizukuManager.isShizukuRunningAndGranted() || RishManager.isRishAvailable();
        boolean hasRoot = ShellExecutor.isRootSuAvailable();
        boolean hasElevatedAccess = hasShizuku || hasRoot;

        if (hasRoot) {
            holder.tvBadge.setText("[ROOT / SUPERUSER]");
            holder.tvBadge.setTextColor(Color.parseColor("#00F0FF"));
        } else if (hasShizuku) {
            holder.tvBadge.setText("[SHIZUKU / PRIVILEGED]");
            holder.tvBadge.setTextColor(Color.parseColor("#00FF66"));
        } else {
            holder.tvBadge.setText("[SHIZUKU / ROOT REQUIRED]");
            holder.tvBadge.setTextColor(Color.parseColor("#FF4444"));
        }

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

                // Elevated Privilege Gate: verify Shizuku, Rish, or Root access
                boolean privileged = ShizukuManager.isShizukuRunningAndGranted()
                        || RishManager.isRishAvailable()
                        || ShellExecutor.isRootSuAvailable();

                if (isChecked && !privileged) {
                    holder.isUpdatingProgrammatically = true;
                    buttonView.setChecked(false);
                    targetItem.setApplied(false);
                    TweakPreferences.saveTweakState(context, targetItem.getId(), false);
                    holder.isUpdatingProgrammatically = false;
                    ShizukuManager.showShizukuPermissionDialog(context, targetItem.getTitle());
                    return;
                }

                // 1. Immediately persist state in memory & storage
                targetItem.setApplied(isChecked);
                TweakPreferences.saveTweakState(context, targetItem.getId(), isChecked);

                if (stateChangeListener != null) {
                    int appliedCount = TweakManagerRepository.getAppliedCount(context);
                    stateChangeListener.onTweakStateChanged(targetItem, isChecked, appliedCount);
                }

                // 2. Offload execution to background AppExecutors worker thread
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        TweakManagerRepository.applyTweak(context, targetItem);
                    } else {
                        TweakManagerRepository.revertTweak(context, targetItem);
                    }
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
