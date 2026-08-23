package com.gamebooster.app.spoofer.ui;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofPreferences;
import com.gamebooster.app.spoofer.SpoofProfile;
import com.gamebooster.app.spoofer.SpoofProfileRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PerAppSpoofAdapter — RecyclerView adapter for per-app spoof device assignment.
 */
public class PerAppSpoofAdapter extends RecyclerView.Adapter<PerAppSpoofAdapter.ViewHolder> {

    public interface OnProfileAssignedListener {
        void onProfileAssigned(String packageName, String profileId);
    }

    public static class AppItem {
        public final String packageName;
        public final String appName;
        public final Drawable icon;

        public AppItem(String packageName, String appName, Drawable icon) {
            this.packageName = packageName;
            this.appName = appName;
            this.icon = icon;
        }
    }

    private final Context context;
    private final List<AppItem> appList;
    private final OnProfileAssignedListener listener;

    public PerAppSpoofAdapter(Context context, List<AppItem> appList, OnProfileAssignedListener listener) {
        this.context = context;
        this.appList = appList != null ? appList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_per_app_spoof, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppItem item = appList.get(position);
        holder.tvAppName.setText(item.appName);

        if (item.icon != null) {
            holder.ivIcon.setImageDrawable(item.icon);
        } else {
            holder.ivIcon.setImageResource(R.mipmap.ic_launcher);
        }

        String assignedId = SpoofPreferences.getProfileIdForPackage(context, item.packageName);
        if (assignedId != null && !assignedId.isEmpty()) {
            SpoofProfile p = DeviceSpooferEngine.getProfileById(assignedId);
            if (p != null) {
                holder.tvAssignedProfile.setText("⚡ " + p.displayName + " (" + p.model + ")");
                holder.tvAssignedProfile.setTextColor(context.getColor(R.color.accent_neon_green));
            } else {
                holder.tvAssignedProfile.setText("⚡ Custom: " + assignedId);
                holder.tvAssignedProfile.setTextColor(context.getColor(R.color.accent_cyan));
            }
        } else {
            String globalId = SpoofPreferences.getActiveProfileId(context);
            SpoofProfile gp = globalId != null ? DeviceSpooferEngine.getProfileById(globalId) : null;
            String globalName = gp != null ? gp.displayName : "Global Active Profile";
            holder.tvAssignedProfile.setText("Default (" + globalName + ")");
            holder.tvAssignedProfile.setTextColor(context.getColor(R.color.text_secondary));
        }

        holder.btnChangeProfile.setOnClickListener(v -> showProfileSelectionDialog(item));
        holder.itemView.setOnClickListener(v -> showProfileSelectionDialog(item));
    }

    private void showProfileSelectionDialog(AppItem item) {
        List<SpoofProfile> allProfiles = new ArrayList<>(SpoofProfileRegistry.getAllProfiles().values());
        Collections.sort(allProfiles, (a, b) -> a.displayName.compareToIgnoreCase(b.displayName));

        List<String> displayItems = new ArrayList<>();
        displayItems.add("🔄 [USE GLOBAL ACTIVE PROFILE] (Reset)");

        for (SpoofProfile p : allProfiles) {
            displayItems.add("📱 " + p.displayName + " [" + p.model + " - " + p.maxRefreshRateHz + "Hz]");
        }

        CharSequence[] charSeqItems = displayItems.toArray(new CharSequence[0]);

        new AlertDialog.Builder(context)
                .setTitle("Select Spoof Device for " + item.appName)
                .setItems(charSeqItems, (dialog, which) -> {
                    if (which == 0) {
                        // Reset to global
                        SpoofPreferences.clearProfileForPackage(context, item.packageName);
                        notifyDataSetChanged();
                        if (listener != null) listener.onProfileAssigned(item.packageName, null);
                    } else {
                        SpoofProfile chosen = allProfiles.get(which - 1);
                        SpoofPreferences.setProfileIdForPackage(context, item.packageName, chosen.id);
                        notifyDataSetChanged();
                        if (listener != null) listener.onProfileAssigned(item.packageName, chosen.id);
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvAppName;
        final TextView tvAssignedProfile;
        final Button btnChangeProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_app_icon);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            tvAssignedProfile = itemView.findViewById(R.id.tv_assigned_profile);
            btnChangeProfile = itemView.findViewById(R.id.btn_change_profile);
        }
    }
}
