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

    public void notifyAllStatesChanged() {
        if (context != null) {
            TweakPreferences.loadSavedStates(context, allMasterTweaks);
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
                    } else if (currentQuery.equals("fps") && (title.contains("fps") || title.contains("185hz") || title.contains("frame rate") || desc.contains("fps") || desc.contains("refresh rate"))) {
                        matches = true;
                    } else if (currentQuery.equals("vulkan") && (title.contains("vulkan") || desc.contains("vulkan"))) {
                        matches = true;
                    } else if ((currentQuery.equals("touch") || currentQuery.equals("aim") || currentQuery.equals("sampling")) && (title.contains("touch") || desc.contains("touch") || desc.contains("latency") || title.contains("digitizer") || title.contains("pressure") || title.contains("slop"))) {
                        matches = true;
                    } else if (currentQuery.equals("gyro") && (title.contains("gyro") || desc.contains("gyro") || desc.contains("recoil"))) {
                        matches = true;
                    } else if ((currentQuery.equals("cpu") || currentQuery.equals("kernel")) && (title.contains("cpu") || title.contains("cfs") || title.contains("sched") || title.contains("kernel") || desc.contains("cpu") || desc.contains("kernel"))) {
                        matches = true;
                    } else if ((currentQuery.equals("gpu") || currentQuery.equals("graphics")) && (title.contains("gpu") || title.contains("adreno") || title.contains("mali") || title.contains("vulkan") || desc.contains("gpu") || desc.contains("graphics"))) {
                        matches = true;
                    } else if ((currentQuery.equals("net") || currentQuery.equals("ping") || currentQuery.equals("wifi") || currentQuery.equals("dns")) && (title.contains("tcp") || title.contains("wi-fi") || title.contains("dns") || title.contains("packet") || desc.contains("ping") || desc.contains("network"))) {
                        matches = true;
                    } else if (currentQuery.equals("audio") && (title.contains("audio") || desc.contains("audio") || desc.contains("gunshot"))) {
                        matches = true;
                    } else if ((currentQuery.equals("damage") || currentQuery.equals("dmg") || currentQuery.equals("crit")) && (title.contains("damage") || desc.contains("damage") || desc.contains("crit") || id.contains("damage"))) {
                        matches = true;
                    } else if ((currentQuery.equals("headshot") || currentQuery.equals("head") || currentQuery.equals("longshot")) && (title.contains("headshot") || desc.contains("headshot") || desc.contains("bullet spread") || id.contains("headshot"))) {
                        matches = true;
                    } else if ((currentQuery.equals("lock") || currentQuery.equals("target") || currentQuery.equals("hero")) && (title.contains("lock") || title.contains("target") || desc.contains("target") || desc.contains("hero") || id.contains("target_lock"))) {
                        matches = true;
                    } else if ((currentQuery.equals("drone") || currentQuery.equals("fov") || currentQuery.equals("camera")) && (title.contains("drone") || desc.contains("fov") || desc.contains("camera") || id.contains("drone_view"))) {
                        matches = true;
                    } else if ((currentQuery.equals("cooldown") || currentQuery.equals("cdr") || currentQuery.equals("skill")) && (title.contains("cooldown") || desc.contains("cooldown") || desc.contains("cdr") || id.contains("fast_cooldown"))) {
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
        boolean hasElevatedAccess = hasShizuku;

        if (hasShizuku) {
            holder.tvBadge.setText("[SHIZUKU ACTIVE / FULL NATIVE]");
            holder.tvBadge.setTextColor(Color.parseColor("#00FF66"));
        } else {
            holder.tvBadge.setText("[SHIZUKU ACCESS REQUIRED]");
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

                if (isChecked && !ShizukuManager.isShizukuRunningAndGranted() && !RishManager.isRishAvailable()) {
                    holder.isUpdatingProgrammatically = true;
                    buttonView.setChecked(false);
                    holder.isUpdatingProgrammatically = false;
                    targetItem.setApplied(false);
                    TweakPreferences.saveTweakState(context, targetItem.getId(), false);
                    if (stateChangeListener != null) {
                        stateChangeListener.onTweakStateChanged(targetItem, false, TweakManagerRepository.getAppliedCount(context));
                    }
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
