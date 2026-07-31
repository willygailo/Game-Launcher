package com.gamebooster.app.ui.layout;

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
import com.gamebooster.app.core.EngineUIHelper;
import com.gamebooster.app.functions.TweakCategory;
import com.gamebooster.app.functions.TweakManagerRepository;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;

public class TweaksFragment extends Fragment implements ShizukuManager.ShizukuStateListener {

    private TweaksAdapter adapter;
    private TextView tvStatus;
    private View bannerDisconnect;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tweaks, container, false);

        tvStatus = view.findViewById(R.id.tv_tweaks_status);
        bannerDisconnect = view.findViewById(R.id.banner_shizuku_disconnect);
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
    public void onStart() {
        super.onStart();
        ShizukuManager.addStateListener(this);
        boolean alive = ShizukuExecutor.hasShizukuPermission();
        onBinderStateChanged(alive);
    }

    @Override
    public void onStop() {
        super.onStop();
        ShizukuManager.removeStateListener(this);
    }

    @Override
    public void onBinderStateChanged(boolean alive) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (adapter != null) {
                    adapter.setShizukuAlive(alive);
                }
                if (bannerDisconnect != null) {
                    bannerDisconnect.setVisibility(alive ? View.GONE : View.VISIBLE);
                }
                EngineUIHelper.refreshEngineStatus(tvStatus);
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EngineUIHelper.refreshEngineStatus(tvStatus);
        boolean alive = ShizukuExecutor.hasShizukuPermission();
        if (adapter != null) {
            adapter.setShizukuAlive(alive);
        }
        if (bannerDisconnect != null) {
            bannerDisconnect.setVisibility(alive ? View.GONE : View.VISIBLE);
        }
    }
}
