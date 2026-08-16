package com.gamebooster.app.ui.screens;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.spoofer.SpoofProfile;

import java.util.ArrayList;
import java.util.List;

public class SpoofProfileAdapter extends RecyclerView.Adapter<SpoofProfileAdapter.ViewHolder> {

    public interface OnProfileClickListener {
        void onProfileClick(SpoofProfile profile);
    }

    private final Context context;
    private final List<SpoofProfile> profiles = new ArrayList<>();
    private final OnProfileClickListener listener;
    private String activeProfileId = null;

    public SpoofProfileAdapter(Context context, List<SpoofProfile> initialProfiles, OnProfileClickListener listener) {
        this.context = context;
        if (initialProfiles != null) {
            this.profiles.addAll(initialProfiles);
        }
        this.listener = listener;
    }

    public void updateProfiles(List<SpoofProfile> newProfiles) {
        this.profiles.clear();
        if (newProfiles != null) {
            this.profiles.addAll(newProfiles);
        }
        notifyDataSetChanged();
    }

    public void setActiveProfileId(String activeProfileId) {
        this.activeProfileId = activeProfileId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_spoof_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpoofProfile profile = profiles.get(position);
        holder.tvDeviceName.setText(profile.displayName);
        int ramGb = profile.ramTotalMb / 1024;
        holder.tvDeviceDetails.setText(profile.model + " • " + profile.socModel + " • " + profile.glRenderer + " • " + ramGb + "GB RAM");

        boolean isActive = profile.id.equals(activeProfileId);
        holder.tvActiveBadge.setVisibility(isActive ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProfileClick(profile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;
        TextView tvDeviceDetails;
        TextView tvActiveBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            tvDeviceDetails = itemView.findViewById(R.id.tv_device_details);
            tvActiveBadge = itemView.findViewById(R.id.tv_active_badge);
        }
    }
}
