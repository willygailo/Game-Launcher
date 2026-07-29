package com.gamespace.app.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamespace.app.R;
import com.gamespace.app.tweaks.TweakItem;
import com.gamespace.app.tweaks.TweakManagerRepository;

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

        if (tweak.isRequiresShizuku()) {
            holder.tvBadge.setText("[SHIZUKU/ADB]");
            holder.tvBadge.setTextColor(Color.parseColor("#00FF66"));
        } else {
            holder.tvBadge.setText("[SYSTEM SETTINGS]");
            holder.tvBadge.setTextColor(Color.parseColor("#00F0FF"));
        }

        holder.switchToggle.setOnCheckedChangeListener(null);
        holder.switchToggle.setChecked(tweak.isApplied());

        holder.switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean success;
            if (isChecked) {
                success = TweakManagerRepository.applyTweak(tweak);
                if (success) {
                    Toast.makeText(context, tweak.getTitle() + " APPLIED", Toast.LENGTH_SHORT).show();
                } else {
                    holder.switchToggle.setChecked(false);
                    Toast.makeText(context, "Permission Denied for " + tweak.getTitle(), Toast.LENGTH_SHORT).show();
                }
            } else {
                success = TweakManagerRepository.revertTweak(tweak);
                if (success) {
                    Toast.makeText(context, tweak.getTitle() + " REVERTED", Toast.LENGTH_SHORT).show();
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

        public TweakViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBadge = itemView.findViewById(R.id.tv_tweak_badge);
            tvTitle = itemView.findViewById(R.id.tv_tweak_title);
            tvDescription = itemView.findViewById(R.id.tv_tweak_description);
            switchToggle = itemView.findViewById(R.id.switch_tweak_toggle);
        }
    }
}
