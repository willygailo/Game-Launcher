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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gamebooster.app.R;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuHealthMonitor;
import com.gamebooster.app.shizuku.role.RoleManager;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TerminalFragment — Standalone, clean in-app Shizuku terminal for Game Booster Pro.
 *
 * Provides:
 *  - Real-time Shizuku status indicator
 *  - High-performance full screen console output window
 *  - Arbitrary shell command input bar powered directly by Shizuku ADB binder execution
 *  - Role-based command execution enforcement
 *  - Clear exit navigation & back press dispatcher support
 */
public class TerminalFragment extends Fragment {

    private static final String TAG = "TerminalFragment";

    private TextView mOutputView;
    private ScrollView mOutputScroll;
    private TextView mStatusView;
    private TextInputEditText mCommandInput;
    private Button mBtnRunCommand;
    private Button mBtnClear;
    private Button mBtnExit;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final StringBuilder mOutputBuffer = new StringBuilder();
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private final List<String> mCommandHistory = new ArrayList<>();

    // Health monitor listener for live Shizuku status update
    private final ShizukuHealthMonitor.HealthListener mHealthListener = health -> {
        if (mStatusView != null && isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (mStatusView != null) {
                    mStatusView.setText(health.getFullLabel());
                }
                updateButtonStates();
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

        mOutputView    = view.findViewById(R.id.tv_terminal_output);
        mOutputScroll  = view.findViewById(R.id.sv_terminal_output);
        mStatusView    = view.findViewById(R.id.tv_shizuku_status);
        mCommandInput  = view.findViewById(R.id.et_command_input);
        mBtnRunCommand = view.findViewById(R.id.btn_run_command);
        mBtnClear      = view.findViewById(R.id.btn_clear_output);
        mBtnExit       = view.findViewById(R.id.btn_exit_terminal);

        // Register system back button callback so user can cleanly exit terminal
        if (getActivity() != null) {
            getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    exitTerminal();
                }
            });
        }

        // Header Exit button listener
        if (mBtnExit != null) {
            mBtnExit.setOnClickListener(v -> exitTerminal());
        }

        // Quick Preset Chips
        bindPresetChip(view, R.id.chip_cmd_getprop, "getprop ro.build.version.release");
        bindPresetChip(view, R.id.chip_cmd_wmsize, "wm size");
        bindPresetChip(view, R.id.chip_cmd_dumpsys, "dumpsys display | grep -E \"mBaseDisplayInfo|DisplayDeviceInfo\"");
        bindPresetChip(view, R.id.chip_cmd_pmlist, "pm list packages -3");

        updateButtonStates();

        // Shizuku health status subscription
        if (getContext() != null) {
            ShizukuHealthMonitor monitor = ShizukuHealthMonitor.getInstance();
            monitor.addListener(mHealthListener);
            monitor.start(getContext());
            monitor.forceCheck();
        }

        // Clear log listener
        if (mBtnClear != null) {
            mBtnClear.setOnClickListener(v -> {
                mOutputBuffer.setLength(0);
                if (mOutputView != null) {
                    mOutputView.setText("[GameBooster Terminal] Output cleared.\n");
                }
            });
        }

        // Custom command listener
        if (mBtnRunCommand != null) {
            mBtnRunCommand.setOnClickListener(v -> runCustomCommand());
        }
        if (mCommandInput != null) {
            mCommandInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    runCustomCommand();
                    return true;
                }
                return false;
            });
        }
    }

    private void bindPresetChip(View parent, int chipId, String commandText) {
        Button chip = parent.findViewById(chipId);
        if (chip != null) {
            chip.setOnClickListener(v -> {
                if (mCommandInput != null) {
                    mCommandInput.setText(commandText);
                    mCommandInput.setSelection(commandText.length());
                }
            });
        }
    }

    private void exitTerminal() {
        if (isAdded() && getParentFragmentManager() != null) {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ShizukuHealthMonitor.getInstance().removeListener(mHealthListener);
        mExecutor.shutdown();
    }

    // -----------------------------------------------------------------------------------------
    // Command Execution Logic
    // -----------------------------------------------------------------------------------------

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
        if (mOutputView != null) {
            mOutputView.setText(mOutputBuffer.toString());
        }
        if (mOutputScroll != null) {
            mOutputScroll.post(() -> mOutputScroll.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void updateButtonStates() {
        if (getContext() == null) return;
        RoleManager rm = RoleManager.getInstance(getContext());
        boolean canUse = rm.canUseTerminal();
        if (mBtnRunCommand != null) mBtnRunCommand.setEnabled(canUse);
        if (mCommandInput != null) {
            mCommandInput.setEnabled(canUse);
            if (!canUse) {
                mCommandInput.setHint("Connect Shizuku to execute commands");
            } else {
                mCommandInput.setHint("Enter ADB command...");
            }
        }
    }
}
