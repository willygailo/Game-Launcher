package com.gamebooster.app.cleaner.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.cleaner.model.JunkCategory;
import com.gamebooster.app.cleaner.model.JunkScanResult;

import java.util.ArrayList;
import java.util.List;

public class JunkCategoryAdapter extends RecyclerView.Adapter<JunkCategoryAdapter.ViewHolder> {

    public interface OnCategorySelectionChangedListener {
        void onSelectionChanged();
    }

    private final Context context;
    private final JunkScanResult scanResult;
    private final List<JunkCategory> categories = new ArrayList<>();
    private final OnCategorySelectionChangedListener listener;

    public JunkCategoryAdapter(Context context, JunkScanResult scanResult, OnCategorySelectionChangedListener listener) {
        this.context = context;
        this.scanResult = scanResult;
        this.listener = listener;

        if (scanResult != null) {
            for (JunkCategory cat : JunkCategory.values()) {
                // Show categories that have items or are system cache
                if (scanResult.getCategorySize(cat) > 0 || scanResult.getCategoryCount(cat) > 0 || cat == JunkCategory.SYSTEM_CACHE) {
                    categories.add(cat);
                }
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_junk_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JunkCategory category = categories.get(position);
        long categorySize = scanResult != null ? scanResult.getCategorySize(category) : 0;
        int categoryCount = scanResult != null ? scanResult.getCategoryCount(category) : 0;

        holder.tvIcon.setText(category.getIconEmoji());
        holder.tvTitle.setText(category.getTitle());
        holder.tvDesc.setText(category.getDescription());
        holder.tvSize.setText(JunkScanResult.formatBytes(categorySize));
        holder.tvCount.setText(categoryCount > 0 ? "(" + categoryCount + " items)" : "(System Trim)");

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(scanResult != null && scanResult.isCategorySelected(category));

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (scanResult != null) {
                scanResult.setCategorySelected(category, isChecked);
            }
            if (listener != null) {
                listener.onSelectionChanged();
            }
        });

        holder.itemView.setOnClickListener(v -> holder.cbSelect.toggle());
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvIcon;
        final TextView tvTitle;
        final TextView tvDesc;
        final TextView tvSize;
        final TextView tvCount;
        final CheckBox cbSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tv_category_icon);
            tvTitle = itemView.findViewById(R.id.tv_category_title);
            tvDesc = itemView.findViewById(R.id.tv_category_desc);
            tvSize = itemView.findViewById(R.id.tv_category_size);
            tvCount = itemView.findViewById(R.id.tv_category_count);
            cbSelect = itemView.findViewById(R.id.cb_category_select);
        }
    }
}
