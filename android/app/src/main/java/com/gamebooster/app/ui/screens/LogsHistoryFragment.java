package com.gamebooster.app.ui.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.core.LogItem;
import com.gamebooster.app.core.OptimizationLogRepository;
import com.gamebooster.app.core.OptimizationRestoreManager;

import java.util.ArrayList;
import java.util.List;

/**
 * LogsHistoryFragment — Displays optimization action history, timestamps, success/fail metrics,
 * and provides a single-tap UNDO / RESTORE button to revert all settings to AOSP defaults.
 */
public class LogsHistoryFragment extends Fragment {

    private RecyclerView rvLogs;
    private LogsAdapter adapter;
    private TextView tvEmptyLogs;
    private final List<LogItem> logList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logs_history, container, false);

        Button btnUndo = view.findViewById(R.id.btn_undo_all_settings);
        Button btnClearLogs = view.findViewById(R.id.btn_clear_logs);
        tvEmptyLogs = view.findViewById(R.id.tv_empty_logs);
        rvLogs = view.findViewById(R.id.rv_logs_list);

        rvLogs.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LogsAdapter(logList);
        rvLogs.setAdapter(adapter);

        if (btnUndo != null) {
            btnUndo.setOnClickListener(v -> {
                if (getContext() == null) return;
                btnUndo.setEnabled(false);
                Toast.makeText(getContext(), "🔄 Reverting all optimizations to AOSP defaults...", Toast.LENGTH_SHORT).show();

                AppExecutors.getInstance().executeCommand(() -> {
                    boolean success = OptimizationRestoreManager.restorePreviousSystemState(getContext());
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            btnUndo.setEnabled(true);
                            if (success) {
                                Toast.makeText(getContext(), "✅ Previous settings restored successfully!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "❌ Undo failed or no backup found", Toast.LENGTH_LONG).show();
                            }
                            loadLogs();
                        }
                    });
                });
            });
        }

        if (btnClearLogs != null) {
            btnClearLogs.setOnClickListener(v -> {
                if (getContext() != null) {
                    OptimizationLogRepository.clearLogs(getContext());
                    loadLogs();
                    Toast.makeText(getContext(), "Logs history cleared", Toast.LENGTH_SHORT).show();
                }
            });
        }

        loadLogs();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLogs();
    }

    private void loadLogs() {
        if (getContext() == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            List<LogItem> logs = OptimizationLogRepository.getLogs(getContext());
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                logList.clear();
                logList.addAll(logs);
                if (adapter != null) adapter.notifyDataSetChanged();
                if (tvEmptyLogs != null) {
                    tvEmptyLogs.setVisibility(logList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
        });
    }

    private static class LogsAdapter extends RecyclerView.Adapter<LogViewHolder> {
        private final List<LogItem> logs;

        LogsAdapter(List<LogItem> logs) {
            this.logs = logs;
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log_entry, parent, false);
            return new LogViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            LogItem log = logs.get(position);
            holder.tvAction.setText(log.actionName);
            holder.tvTime.setText(log.getFormattedTime());
            holder.tvDesc.setText(log.description);
            holder.tvValueChange.setText("Previous: " + log.previousValue + " ➔ New: " + log.newValue);

            if (log.success) {
                holder.tvStatus.setText("✓ SUCCESS");
                holder.tvStatus.setTextColor(0xFF00FF66); // Neon Green
            } else {
                holder.tvStatus.setText("✕ FAILED");
                holder.tvStatus.setTextColor(0xFFFF0055); // Red
            }
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }
    }

    private static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvAction;
        TextView tvTime;
        TextView tvStatus;
        TextView tvDesc;
        TextView tvValueChange;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAction = itemView.findViewById(R.id.tv_log_action);
            tvTime = itemView.findViewById(R.id.tv_log_time);
            tvStatus = itemView.findViewById(R.id.tv_log_status);
            tvDesc = itemView.findViewById(R.id.tv_log_desc);
            tvValueChange = itemView.findViewById(R.id.tv_log_value_change);
        }
    }
}
