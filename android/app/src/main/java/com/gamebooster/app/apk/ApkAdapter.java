package com.gamebooster.app.apk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.games.GameLauncherHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApkAdapter extends RecyclerView.Adapter<ApkAdapter.ApkViewHolder> {

    public interface OnApkActionListener {
        void onInstallRequested(ApkItem item);
        void onApkDeleted(ApkItem item);
        void onAddedToHome(ApkItem item);
    }

    private final Context context;
    private final List<ApkItem> items = new ArrayList<>();
    private final OnApkActionListener listener;

    public ApkAdapter(Context context, List<ApkItem> initialList, OnApkActionListener listener) {
        this.context = context;
        if (initialList != null) {
            this.items.addAll(initialList);
        }
        this.listener = listener;
    }

    public void updateList(List<ApkItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ApkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_apk_card, parent, false);
        return new ApkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApkViewHolder holder, int position) {
        ApkItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ApkViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivIcon;
        private final TextView tvName;
        private final TextView tvPkg;
        private final TextView tvVersionSize;
        private final TextView tvStatus;
        private final Button btnInstall;
        private final Button btnAddHome;
        private final Button btnDelete;

        public ApkViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_apk_icon);
            tvName = itemView.findViewById(R.id.tv_apk_name);
            tvPkg = itemView.findViewById(R.id.tv_apk_pkg);
            tvVersionSize = itemView.findViewById(R.id.tv_apk_version_size);
            tvStatus = itemView.findViewById(R.id.tv_apk_installed_status);
            btnInstall = itemView.findViewById(R.id.btn_apk_install);
            btnAddHome = itemView.findViewById(R.id.btn_apk_add_home);
            btnDelete = itemView.findViewById(R.id.btn_apk_delete);
        }

        public void bind(ApkItem item) {
            tvName.setText(item.getAppName());
            tvPkg.setText(item.getPackageName());

            String sizeStr = formatFileSize(item.getFileSizeBytes());
            tvVersionSize.setText("v" + item.getVersionName() + " • " + sizeStr);

            if (item.getIcon() != null) {
                ivIcon.setImageDrawable(item.getIcon());
            } else {
                ivIcon.setImageResource(R.drawable.badge_neon_cyan);
            }

            if (item.isInstalled()) {
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("INSTALLED");
                tvStatus.setBackgroundResource(R.drawable.badge_neon_green);
                tvStatus.setTextColor(0xFF00FF66);
                btnInstall.setText("⚡ UPDATE");
            } else {
                tvStatus.setVisibility(View.GONE);
                btnInstall.setText("⚡ INSTALL");
            }

            btnInstall.setOnClickListener(v -> {
                if (listener != null) listener.onInstallRequested(item);
            });

            btnAddHome.setOnClickListener(v -> {
                GameLauncherHelper.addCustomPackage(context, item.getPackageName());
                Toast.makeText(context, "Added " + item.getAppName() + " to HOME games!", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onAddedToHome(item);
            });

            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("🗑️ DELETE APK")
                        .setMessage("Delete installation file:\n" + item.getFilePath() + "?")
                        .setPositiveButton("DELETE", (d, w) -> {
                            boolean deleted = ApkInstallerEngine.deleteApkFile(item.getFilePath());
                            if (deleted) {
                                Toast.makeText(context, "Deleted APK: " + item.getAppName(), Toast.LENGTH_SHORT).show();
                                if (listener != null) listener.onApkDeleted(item);
                            } else {
                                Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
            });
        }

        private String formatFileSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
            return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
