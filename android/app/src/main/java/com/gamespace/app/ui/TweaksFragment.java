package com.gamespace.app.ui;

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

import com.gamespace.app.R;
import com.gamespace.app.core.EngineMode;
import com.gamespace.app.data.CommandExecutor;
import com.gamespace.app.tweaks.TweakCategory;
import com.gamespace.app.tweaks.TweakManagerRepository;

public class TweaksFragment extends Fragment {

    private TweaksAdapter adapter;
    private TextView tvStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweaks, container, false);

        tvStatus = view.findViewById(R.id.tv_tweaks_status);
        Button btnApplyAll = view.findViewById(R.id.btn_apply_all_tweaks);
        RecyclerView rvTweaks = view.findViewById(R.id.rv_tweaks_list);

        Button btnFilterAll = view.findViewById(R.id.btn_filter_all);
        Button btnFilterCpuGpu = view.findViewById(R.id.btn_filter_cpugpu);
        Button btnFilterTouch = view.findViewById(R.id.btn_filter_touch);
        Button btnFilterRoot = view.findViewById(R.id.btn_filter_root);
        Button btnFilterShizuku = view.findViewById(R.id.btn_filter_shizuku);

        rvTweaks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TweaksAdapter(getContext(), TweakManagerRepository.getAllTweaks());
        rvTweaks.setAdapter(adapter);

        updateEngineStatus();

        btnApplyAll.setOnClickListener(v -> {
            int appliedCount = TweakManagerRepository.applyAllSupportedTweaks();
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Applied " + appliedCount + " system & kernel tweaks!", Toast.LENGTH_SHORT).show();
        });

        btnFilterAll.setOnClickListener(v -> adapter.updateList(TweakManagerRepository.getAllTweaks()));
        btnFilterCpuGpu.setOnClickListener(v -> adapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.CPU_GPU)));
        btnFilterTouch.setOnClickListener(v -> adapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.TOUCH_DISPLAY)));
        btnFilterRoot.setOnClickListener(v -> adapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.ROOT_KERNEL)));
        btnFilterShizuku.setOnClickListener(v -> adapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.SHIZUKU_SYSTEM)));

        return view;
    }

    private void updateEngineStatus() {
        EngineMode mode = CommandExecutor.getActiveEngineMode();
        tvStatus.setText("Active Engine: " + mode.getDisplayName());
        tvStatus.setTextColor(mode.getColorHex());
    }
}
