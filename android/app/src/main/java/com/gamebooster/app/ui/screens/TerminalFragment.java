package com.gamebooster.app.ui.screens;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gamebooster.app.R;
import com.gamebooster.app.shizuku.ShizukuAutoStarter;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileBridge;
import com.gamebooster.app.shizuku.ShizukuForceApplyEngine;
import com.gamebooster.app.shizuku.ShizukuHealthMonitor;
import com.gamebooster.app.shizuku.ShizukuTerminalManager;
import com.gamebooster.app.shizuku.role.RoleManager;
import com.gamebooster.app.shizuku.role.ShizukuRole;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TerminalFragment — In-app Shizuku terminal for Game Booster Pro.
 *
 * Provides:
 *  - Real-time Shizuku health status display
 *  - Preset quick-action buttons (Start Shizuku, Grant All, Force Apply, Game Folders, Device Info)
 *  - Custom command input field (ADMIN role only)
 *  - Scrollable output window with timestamps
 *  - Role-based access control enforcement (READONLY cannot run commands)
 */
public class TerminalFragment extends Fragment {

    private static final String TAG = "TerminalFragment";

    private TextView mOutputView;
    private ScrollView mOutputScroll;
    private TextView mStatusView;
    private TextInputEditText mCommandInput;
    private Button mBtnRunCommand;
    private Button mBtnStartShizuku;
    private Button mBtnGrantAll;
    private Button mBtnForceApply;
    private Button mBtnListFolders;
    private Button mBtnDeviceInfo;
    private Button mBtnClear;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final StringBuilder mOutputBuffer = new StringBuilder();
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    // Health monitor listener
    private final ShizukuHealthMonitor.HealthListener mHealthListener = health -> {
        if (mStatusView != null && isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (mStatusView != null) {
                    mStatusView.setText(health.getFullLabel());
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_terminal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mOutputView      = view.findViewById(R.id.tv_terminal_output);
        mOutputScroll    = view.findViewById(R.id.sv_terminal_output);
        mStatusView      = view.findViewById(R.id.tv_shizuku_status);
        mCommandInput    = view.findViewById(R.id.et_command_input);
        mBtnRunCommand   = view.findViewById(R.id.btn_run_command);
        mBtnStartShizuku = view.findViewById(R.id.btn_start_shizuku);
        mBtnGrantAll     = view.findViewById(R.id.btn_grant_permissions);
        mBtnForceApply   = view.findViewById(R.id.btn_force_apply);
        mBtnListFolders  = view.findViewById(R.id.btn_list_folders);
        mBtnDeviceInfo   = view.findViewById(R.id.btn_device_info);
        mBtnClear        = view.findViewById(R.id.btn_clear_output);

        // Initial role check
        updateButtonStates();

        // Shizuku health status subscription
        if (getContext() != null) {
            ShizukuHealthMonitor monitor = ShizukuHealthMonitor.getInstance();
            monitor.addListener(mHealthListener);
            monitor.start(getContext());
            monitor.forceCheck();
        }

        // Button listeners
        mBtnStartShizuku.setOnClickListener(v -> runPreset("▶ Start Shizuku", () -> {
            if (getContext() == null) return "❌ Context unavailable";
            ShizukuAutoStarter.StartResult res =
                    ShizukuAutoStarter.startShizukuDaemon(getContext());
            return res.success ? "✅ Shizuku started: " + res.output
                    : "❌ Start failed: " + res.output;
        }));

        mBtnGrantAll.setOnClickListener(v -> runPreset("🔑 Grant All Permissions", () -> {
            if (getContext() == null) return "❌ Context unavailable";
            ShizukuExecutor.GrantResult res =
                    ShizukuExecutor.grantAppPermissionsViaShizuku(getContext());
            return res.success ? "✅ Granted " + res.executedCommands + " permissions"
                    : "❌ Grant failed";
        }));

        mBtnForceApply.setOnClickListener(v -> {
            if (getContext() == null) return;
            RoleManager rm = RoleManager.getInstance(getContext());
            if (!rm.canForceApply()) {
                appendOutput("❌ Force Apply requires ADMIN role. Current: " + rm.getRole());
                return;
            }
            runPreset("⚡ Force Apply", () -> {
                if (getContext() == null) return "❌ Context unavailable";
                ShizukuForceApplyEngine.ForceApplyResult res =
                        ShizukuForceApplyEngine.forceApplyAll(getContext(), 120);
                return res.success ? "✅ Force Apply complete (" + res.totalCommands + " cmds)"
                        : "❌ Force Apply failed: " + res.outputLog;
            });
        });

        mBtnListFolders.setOnClickListener(v -> runPreset("📁 List Game Folders", () -> {
            StringBuilder sb = new StringBuilder();
            String[] paths = {
                    ShizukuFileBridge.MLBB_DATA, ShizukuFileBridge.PUBG_DATA,
                    ShizukuFileBridge.FF_DATA, ShizukuFileBridge.CODM_DATA,
                    ShizukuFileBridge.GENSHIN_DATA
            };
            for (String path : paths) {
                sb.append("\n--- ").append(path).append(" ---\n");
                sb.append(ShizukuFileBridge.listFiles(path)).append("\n");
            }
            return sb.toString();
        }));

        mBtnDeviceInfo.setOnClickListener(v -> runPreset("🔍 Device Info", () -> {
            StringBuilder sb = new StringBuilder();
            sb.append(ShizukuExecutor.executeShizukuCommand("getprop ro.build.version.release")).append("\n");
            sb.append(ShizukuExecutor.executeShizukuCommand("getprop ro.product.model")).append("\n");
            sb.append(ShizukuExecutor.executeShizukuCommand("getprop ro.hardware")).append("\n");
            sb.append(ShizukuExecutor.executeShizukuCommand("cat /sys/class/thermal/thermal_zone0/temp")).append("\n");
            sb.append(ShizukuExecutor.executeShizukuCommand("ls " + ShizukuTerminalManager.TERMINAL_DIR));
            return sb.toString();
        }));

        mBtnClear.setOnClickListener(v -> {
            mOutputBuffer.setLength(0);
            if (mOutputView != null) {
                mOutputView.setText("[GameBooster Terminal] Output cleared.\n");
            }
        });

        // Custom command run
        mBtnRunCommand.setOnClickListener(v -> runCustomCommand());
        mCommandInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                runCustomCommand();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ShizukuHealthMonitor.getInstance().removeListener(mHealthListener);
        mExecutor.shutdown();
    }

    // -----------------------------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------------------------

    private void runPreset(String label, java.util.concurrent.Callable<String> task) {
        appendOutput("\n$ [" + label + "]");
        mExecutor.submit(() -> {
            try {
                String result = task.call();
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> appendOutput(result));
                }
            } catch (Exception e) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> appendOutput("ERROR: " + e.getMessage()));
                }
            }
        });
    }

    private final java.util.List<String> mCommandHistory = new java.util.ArrayList<>();

    private void runCustomCommand() {
        if (mCommandInput == null || getContext() == null) return;
        String cmd = mCommandInput.getText() != null ? mCommandInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(cmd)) return;

        RoleManager rm = RoleManager.getInstance(getContext());
        if (!rm.canUseTerminal()) {
            appendOutput("❌ Terminal access requires ADMIN role. Current: " + rm.getRole().getDisplayName());
            return;
        }

        if (!mCommandHistory.contains(cmd)) {
            mCommandHistory.add(cmd);
        }

        appendOutput("$ " + cmd);
        mCommandInput.setText("");

        final String cmdFinal = cmd;
        mExecutor.submit(() -> {
            String result = ShizukuExecutor.executeShizukuCommand(cmdFinal);
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> appendOutput(result));
            }
        });
    }

    private void appendOutput(String text) {
        String timestamp = "[" + mTimeFormat.format(new Date()) + "] ";
        mOutputBuffer.append(timestamp).append(text).append("\n");
        mOutputView.setText(mOutputBuffer.toString());
        // Auto-scroll to bottom
        mOutputScroll.post(() -> mOutputScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void updateButtonStates() {
        if (getContext() == null) return;
        RoleManager rm = RoleManager.getInstance(getContext());
        boolean canCmd = rm.canForceApply();
        if (mBtnForceApply != null) mBtnForceApply.setEnabled(canCmd);
        if (mBtnRunCommand != null) mBtnRunCommand.setEnabled(rm.canUseTerminal());
        if (mCommandInput != null) {
            mCommandInput.setEnabled(rm.canUseTerminal());
            if (!rm.canUseTerminal()) {
                mCommandInput.setHint("Connect Shizuku to execute commands");
            } else {
                mCommandInput.setHint("e.g. setprop debug.sf.latch_unsignaled 1");
            }
        }
    }
}
