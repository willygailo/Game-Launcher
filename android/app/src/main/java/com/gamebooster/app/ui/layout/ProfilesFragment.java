package com.gamebooster.app.ui.layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.widget.TextView;

import com.gamebooster.app.R;
import com.gamebooster.app.functions.PerformanceChannel;
import com.gamebooster.app.core.EngineUIHelper;

import com.gamebooster.app.core.AppExecutors;

public class ProfilesFragment extends Fragment {

    private TextView tvEngineStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profiles, container, false);

        tvEngineStatus = view.findViewById(R.id.tv_engine_status);
        Button btn3dVulkan = view.findViewById(R.id.btn_engine_3d_vulkan);
        Button btn2dSkia = view.findViewById(R.id.btn_engine_2d_skia);

        Button btnExtreme = view.findViewById(R.id.btn_apply_pubg_profile);
        Button btnPerformance = view.findViewById(R.id.btn_apply_2d_profile);
        Button btnBalanced = view.findViewById(R.id.btn_apply_balanced_profile);

        if (btn3dVulkan != null) {
            btn3dVulkan.setOnClickListener(v -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    boolean ok = PerformanceChannel.setGpuRenderMode(true);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), "⚡ 3D Vulkan HWUI Render Engine Enabled!", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        }

        if (btn2dSkia != null) {
            btn2dSkia.setOnClickListener(v -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    boolean ok = PerformanceChannel.setGpuRenderMode(false);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), "🎮 2D Skia Render Engine Enabled!", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        }

        if (btnExtreme != null) {
            btnExtreme.setOnClickListener(v -> {
                if (getContext() != null) {
                    btnExtreme.setEnabled(false);
                    Toast.makeText(getContext(), "Applying Extreme Performance Profile...", Toast.LENGTH_SHORT).show();
                    AppExecutors.getInstance().executeCommand(() -> {
                        boolean ok = PerformanceChannel.applyProfile(getContext(), PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (!isAdded() || getContext() == null) return;
                            btnExtreme.setEnabled(true);
                            if (ok) {
                                Toast.makeText(getContext(), "🔥 Extreme Performance Profile Applied!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Profile applied with system setting fallbacks", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            });
        }

        if (btnPerformance != null) {
            btnPerformance.setOnClickListener(v -> {
                if (getContext() != null) {
                    btnPerformance.setEnabled(false);
                    Toast.makeText(getContext(), "Applying Performance Profile...", Toast.LENGTH_SHORT).show();
                    AppExecutors.getInstance().executeCommand(() -> {
                        boolean ok = PerformanceChannel.applyProfile(getContext(), PerformanceChannel.Profile.PERFORMANCE);
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (!isAdded() || getContext() == null) return;
                            btnPerformance.setEnabled(true);
                            if (ok) {
                                Toast.makeText(getContext(), "⚡ Performance Profile Applied!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Profile applied with system setting fallbacks", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            });
        }

        if (btnBalanced != null) {
            btnBalanced.setOnClickListener(v -> {
                if (getContext() != null) {
                    btnBalanced.setEnabled(false);
                    Toast.makeText(getContext(), "Applying Balanced Profile...", Toast.LENGTH_SHORT).show();
                    AppExecutors.getInstance().executeCommand(() -> {
                        boolean ok = PerformanceChannel.applyProfile(getContext(), PerformanceChannel.Profile.BALANCED);
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (!isAdded() || getContext() == null) return;
                            btnBalanced.setEnabled(true);
                            if (ok) {
                                Toast.makeText(getContext(), "⚖️ Balanced Game Profile Applied!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Profile applied with system setting fallbacks", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            });
        }

        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
    }
}
