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
import com.gamespace.app.tweaks.TweakCategory;
import com.gamespace.app.tweaks.TweakManagerRepository;
import com.gamespace.app.utils.EngineUIHelper;

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
        Button btnFilterShizuku = view.findViewById(R.id.btn_filter_shizuku);
        Button btnFilterRoot = view.findViewById(R.id.btn_filter_root);

        if (btnFilterRoot != null) {
            btnFilterRoot.setVisibility(View.GONE);
        }

        rvTweaks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TweaksAdapter(getContext(), TweakManagerRepository.getAllTweaks());
        rvTweaks.setAdapter(adapter);

        EngineUIHelper.refreshEngineStatus(tvStatus);

        btnApplyAll.setOnClickListener(v -> {
            int appliedCount = TweakManagerRepository.applyAllSupportedTweaks();
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Applied " + appliedCount + " system optimizations!", Toast.LENGTH_SHORT).show();
        });

        btnFilterAll.setOnClickListener(v -> adapter.updateList(TweakManagerRepository.getAllTweaks()));
        btnFilterCpuGpu.setOnClickListener(v -> adapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.CPU_GPU)));
        btnFilterTouch.setOnClickListener(v -> adapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.TOUCH_DISPLAY)));
        btnFilterShizuku.setOnClickListener(v -> adapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.SHIZUKU_SYSTEM)));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EngineUIHelper.refreshEngineStatus(tvStatus);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
