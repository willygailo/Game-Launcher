package com.gamebooster.app.ui.screens;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameLauncherHelper;
import com.gamebooster.app.games.GameManagerRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * AddGameDialogFragment — Material 3 Dialog Picker allowing users to select any installed
 * application to manually add into the Game Launcher library.
 */
public class AddGameDialogFragment extends DialogFragment {

    public interface OnGameAddedListener {
        void onGameAdded(String packageName);
    }

    private OnGameAddedListener listener;
    private AppAdapter adapter;
    private final List<GameAppInfo> allApps = new ArrayList<>();
    private final List<GameAppInfo> filteredApps = new ArrayList<>();

    public static AddGameDialogFragment newInstance() {
        return new AddGameDialogFragment();
    }

    public void setOnGameAddedListener(OnGameAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Dialog_MinWidth);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_game, container, false);

        EditText etSearch = view.findViewById(R.id.et_search_app);
        RecyclerView rvApps = view.findViewById(R.id.rv_apps_picker);
        View btnClose = view.findViewById(R.id.btn_close_dialog);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        rvApps.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AppAdapter(filteredApps, app -> {
            if (getContext() != null) {
                GameLauncherHelper.addCustomPackage(getContext(), app.getPackageName());
                Toast.makeText(getContext(), "🎮 Added " + app.getLabel() + " to Game Launcher!", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onGameAdded(app.getPackageName());
                }
                dismiss();
            }
        });
        rvApps.setAdapter(adapter);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterApps(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        loadApps();
        return view;
    }

    private void loadApps() {
        if (getContext() == null) return;
        AppExecutors.getInstance().executeScan(() -> {
            List<GameAppInfo> apps = GameManagerRepository.getAllInstalledApps(getContext());
            AppExecutors.getInstance().postToMainThread(() -> {
                if (getContext() == null) return;
                allApps.clear();
                allApps.addAll(apps);
                filterApps(null);
            });
        });
    }

    private void filterApps(String query) {
        filteredApps.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredApps.addAll(allApps);
        } else {
            String q = query.toLowerCase().trim();
            for (GameAppInfo app : allApps) {
                if (app.getLabel().toLowerCase().contains(q) || app.getPackageName().toLowerCase().contains(q)) {
                    filteredApps.add(app);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private static class AppAdapter extends RecyclerView.Adapter<AppViewHolder> {
        interface OnAppClickListener {
            void onAppClick(GameAppInfo app);
        }

        private final List<GameAppInfo> apps;
        private final OnAppClickListener clickListener;

        AppAdapter(List<GameAppInfo> apps, OnAppClickListener clickListener) {
            this.apps = apps;
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_game_app, parent, false);
            return new AppViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            GameAppInfo app = apps.get(position);
            holder.tvName.setText(app.getLabel());
            holder.tvPkg.setText(app.getPackageName());
            if (app.getIcon() != null) {
                holder.ivIcon.setImageDrawable(app.getIcon());
            }
            holder.itemView.setOnClickListener(v -> clickListener.onAppClick(app));
        }

        @Override
        public int getItemCount() {
            return apps.size();
        }
    }

    private static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvPkg;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_app_icon);
            tvName = itemView.findViewById(R.id.tv_app_name);
            tvPkg = itemView.findViewById(R.id.tv_app_pkg);
        }
    }
}
