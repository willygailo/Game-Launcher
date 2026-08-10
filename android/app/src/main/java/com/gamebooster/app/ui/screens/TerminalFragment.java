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
        if (mStatusView != null) {
            requireActivity().runOnUiThread(() ->
                    mStatusView.setText(health.getFullLabel()));
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
        ShizukuHealthMonitor monitor = ShizukuHealthMonitor.getInstance();
        monitor.addListener(mHealthListener);
        monitor.start(requireContext());
        monitor.forceCheck();

        // Button listeners
        mBtnStartShizuku.setOnClickListener(v -> runPreset("▶ Start Shizuku", () -> {
            ShizukuAutoStarter.StartResult res =
                    ShizukuAutoStarter.startShizukuDaemon(requireContext());
            return res.success ? "✅ Shizuku started: " + res.output
                    : "❌ Start failed: " + res.output;
        }));

        mBtnGrantAll.setOnClickListener(v -> runPreset("🔑 Grant All Permissions", () -> {
            ShizukuExecutor.GrantResult res =
                    ShizukuExecutor.grantAppPermissionsViaShizuku(requireContext());
            return res.success ? "✅ Granted " + res.executedCommands + " permissions"
                    : "❌ Grant failed";
        }));

        mBtnForceApply.setOnClickListener(v -> {
            RoleManager rm = RoleManager.getInstance(requireContext());
            if (!rm.canForceApply()) {
                appendOutput("❌ Force Apply requires ADMIN role. Current: " + rm.getRole());
                return;
            }
            runPreset("⚡ Force Apply", () -> {
                ShizukuForceApplyEngine.ForceApplyResult res =
                        ShizukuForceApplyEngine.forceApplyAll(requireContext(), 120);
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
            mOutputView.setText("[GameBooster Terminal] Output cleared.\n");
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
                requireActivity().runOnUiThread(() -> appendOutput(result));
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        appendOutput("ERROR: " + e.getMessage()));
            }
        });
    }

    private void runCustomCommand() {
        if (mCommandInput == null) return;
        String cmd = mCommandInput.getText() != null ? mCommandInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(cmd)) return;

        RoleManager rm = RoleManager.getInstance(requireContext());
        if (!rm.canUseTerminal()) {
            appendOutput("❌ Terminal access requires ADMIN role. Current: " + rm.getRole().getDisplayName());
            return;
        }

        appendOutput("$ " + cmd);
        mCommandInput.setText("");

        final String cmdFinal = cmd;
        mExecutor.submit(() -> {
            String result = ShizukuExecutor.executeShizukuCommand(cmdFinal);
            requireActivity().runOnUiThread(() -> appendOutput(result));
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
        RoleManager rm = RoleManager.getInstance(requireContext());
        boolean canCmd = rm.canForceApply();
        mBtnForceApply.setEnabled(canCmd);
        mBtnRunCommand.setEnabled(rm.canUseTerminal());
        mCommandInput.setEnabled(rm.canUseTerminal());
        if (!rm.canUseTerminal()) {
            mCommandInput.setHint("Custom commands require ADMIN role");
        }
    }
}
