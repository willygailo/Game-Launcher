package com.gamebooster.app.ui.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.gamespace.GameSpaceAnalyticsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GameAnalyticsAdapter extends RecyclerView.Adapter<GameAnalyticsAdapter.ViewHolder> {

    private final Context context;
    private final List<GameSpaceAnalyticsManager.GameStatRecord> records = new ArrayList<>();
    private final PackageManager packageManager;

    public GameAnalyticsAdapter(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
    }

    public void setRecords(List<GameSpaceAnalyticsManager.GameStatRecord> list) {
        records.clear();
        if (list != null) {
            records.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game_analytic_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameSpaceAnalyticsManager.GameStatRecord record = records.get(position);

        holder.tvTitle.setText(record.gameLabel);
        holder.tvPkg.setText(record.packageName);
        holder.tvPlaytime.setText(record.getFormattedPlaytime());
        holder.tvSessions.setText("🎮 " + record.totalSessions + " Match" + (record.totalSessions == 1 ? "" : "es"));

        float drainRate = record.getDrainRatePerHour();
        if (drainRate > 0f) {
            holder.tvBatteryDrain.setText(String.format(Locale.US, "🔋 %.1f%% / hr", drainRate));
        } else {
            holder.tvBatteryDrain.setText("🔋 Safe / Shielded");
        }

        // Try resolving app icon
        try {
            Drawable icon = packageManager.getApplicationIcon(record.packageName);
            holder.ivIcon.setImageDrawable(icon);
        } catch (Exception e) {
            holder.ivIcon.setImageResource(R.mipmap.ic_launcher);
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvPkg;
        TextView tvPlaytime;
        TextView tvSessions;
        TextView tvBatteryDrain;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_analytic_game_icon);
            tvTitle = itemView.findViewById(R.id.tv_analytic_game_title);
            tvPkg = itemView.findViewById(R.id.tv_analytic_game_pkg);
            tvPlaytime = itemView.findViewById(R.id.tv_analytic_playtime_badge);
            tvSessions = itemView.findViewById(R.id.tv_analytic_sessions);
            tvBatteryDrain = itemView.findViewById(R.id.tv_analytic_battery_drain);
        }
    }
}
