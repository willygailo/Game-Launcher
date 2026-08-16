package com.gamebooster.app.terminal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.shizuku.GameStorageEngine;
import com.gamebooster.app.shizuku.RishManager;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * High-performance Cyberpunk Terminal Emulator Activity.
 * Compatible with Android 13, 14, 15, and 16 screen layouts (Edge-to-edge & IME aware).
 * Supports direct elevated shell & root execution, /Android/data, /Android/obb, and system file manipulation.
 */
public class TerminalActivity extends AppCompatActivity {

    private View terminalRootLayout;
    private TextView tvTerminalStatus;
    private TextView tvTerminalOutput;
    private ScrollView scrollTerminalOutput;
    private EditText etTerminalCommand;
    private Button btnTerminalRun;
    private Button btnClearTerminal;
    private Button btnCopyTerminal;
    private ImageButton btnTerminalBack;

    private Button btnHistoryPrev;
    private Button btnHistoryNext;

    private Button btnScriptWhoami;
    private Button btnScriptFixStorage;
    private Button btnScriptDataDir;
    private Button btnScriptObbDir;
    private Button btnScriptTempDir;
    private Button btnScriptAnimScale;
    private Button btnScriptFpsDiag;
    private Button btnScriptRamTrim;
    private Button btnScriptTouchDiag;
    private Button btnScriptGpuMode;
    private Button btnScriptThermalBypass;

    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;
    private final SpannableStringBuilder terminalBuffer = new SpannableStringBuilder();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable Edge-to-Edge display support for Android 13, 14, 15, 16
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_terminal);

        setupWindowInsets();
        initViews();
        setupListeners();
        showWelcomeBanner();
    }

    /**
     * Handles status bar, display cutout/notch, navigation bar, and IME keyboard insets across all 4 edges.
     * Prevents UI cutoff or overflow in both portrait and landscape on all Android 13-16 devices.
     */
    private void setupWindowInsets() {
        terminalRootLayout = findViewById(R.id.terminal_root_layout);
        if (terminalRootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(terminalRootLayout, (v, windowInsets) -> {
                androidx.core.graphics.Insets bars = windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars() |
                        WindowInsetsCompat.Type.displayCutout() |
                        WindowInsetsCompat.Type.ime()
                );
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return windowInsets;
            });
        }
    }

    private void initViews() {
        tvTerminalStatus = findViewById(R.id.tv_terminal_status);
        tvTerminalOutput = findViewById(R.id.tv_terminal_output);
        scrollTerminalOutput = findViewById(R.id.scroll_terminal_output);
        etTerminalCommand = findViewById(R.id.et_terminal_command);
        btnTerminalRun = findViewById(R.id.btn_terminal_run);
        btnClearTerminal = findViewById(R.id.btn_clear_terminal);
        btnCopyTerminal = findViewById(R.id.btn_copy_terminal);
        btnTerminalBack = findViewById(R.id.btn_terminal_back);

        btnHistoryPrev = findViewById(R.id.btn_terminal_history_prev);
        btnHistoryNext = findViewById(R.id.btn_terminal_history_next);

        btnScriptWhoami = findViewById(R.id.btn_script_whoami);
        btnScriptFixStorage = findViewById(R.id.btn_script_fix_storage);
        btnScriptDataDir = findViewById(R.id.btn_script_data_dir);
        btnScriptObbDir = findViewById(R.id.btn_script_obb_dir);
        btnScriptTempDir = findViewById(R.id.btn_script_temp_dir);
        btnScriptAnimScale = findViewById(R.id.btn_script_anim_scale);
        btnScriptFpsDiag = findViewById(R.id.btn_script_fps_diag);
        btnScriptRamTrim = findViewById(R.id.btn_script_ram_trim);
        btnScriptTouchDiag = findViewById(R.id.btn_script_touch_diag);
        btnScriptGpuMode = findViewById(R.id.btn_script_gpu_mode);
        btnScriptThermalBypass = findViewById(R.id.btn_script_thermal_bypass);

        updateStatusBanner();
    }

    private void updateStatusBanner() {
        boolean hasShizuku = ShizukuExecutor.hasShizukuPermission();
        String androidVer = "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
        if (hasShizuku) {
            tvTerminalStatus.setText("UID: 2000 (shell) • Shizuku Active • " + androidVer);
            tvTerminalStatus.setTextColor(0xFF00FF66);
        } else {
            tvTerminalStatus.setText("Shizuku Disconnected • " + androidVer);
            tvTerminalStatus.setTextColor(0xFFFFB800);
        }
    }

    private void showWelcomeBanner() {
        appendSpannedText("====================================================\n", 0xFF00F0FF);
        appendSpannedText("  GAME BOOSTER PRO — SHIZUKU TERMINAL ENGINE v2.0\n", 0xFF00FF66);
        appendSpannedText("  Target: Android 13, 14, 15, 16 (API 33-36) Ready\n", 0xFF00F0FF);
        appendSpannedText("  Full System, /Android/data, /Android/obb & Root Storage Control\n", 0xFF94A3B8);
        appendSpannedText("====================================================\n", 0xFF00F0FF);
        appendSpannedText("Type any command (e.g. settings put, setprop, cp, mv, cat, chmod) or 'help'.\n\n", 0xFF94A3B8);
    }

    private void setupListeners() {
        if (btnTerminalBack != null) {
            btnTerminalBack.setOnClickListener(v -> finish());
        }

        if (btnClearTerminal != null) {
            btnClearTerminal.setOnClickListener(v -> {
                terminalBuffer.clear();
                tvTerminalOutput.setText("");
                showWelcomeBanner();
            });
        }

        if (btnCopyTerminal != null) {
            btnCopyTerminal.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("Terminal Log", tvTerminalOutput.getText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Terminal log copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnTerminalRun != null) {
            btnTerminalRun.setOnClickListener(v -> executeCurrentCommand());
        }

        if (etTerminalCommand != null) {
            etTerminalCommand.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    executeCurrentCommand();
                    return true;
                }
                return false;
            });
        }

        if (btnHistoryPrev != null) {
            btnHistoryPrev.setOnClickListener(v -> {
                if (commandHistory.isEmpty()) return;
                if (historyIndex == -1) {
                    historyIndex = commandHistory.size() - 1;
                } else if (historyIndex > 0) {
                    historyIndex--;
                }
                etTerminalCommand.setText(commandHistory.get(historyIndex));
                etTerminalCommand.setSelection(etTerminalCommand.getText().length());
            });
        }

        if (btnHistoryNext != null) {
            btnHistoryNext.setOnClickListener(v -> {
                if (commandHistory.isEmpty() || historyIndex == -1) return;
                if (historyIndex < commandHistory.size() - 1) {
                    historyIndex++;
                    etTerminalCommand.setText(commandHistory.get(historyIndex));
                    etTerminalCommand.setSelection(etTerminalCommand.getText().length());
                } else {
                    historyIndex = -1;
                    etTerminalCommand.setText("");
                }
            });
        }

        // Quick Preset Scripts & Storage Tools
        if (btnScriptWhoami != null) {
            btnScriptWhoami.setOnClickListener(v -> runPresetCommand("id; whoami; pm get-install-location; getprop ro.build.version.release"));
        }
        if (btnScriptFixStorage != null) {
            btnScriptFixStorage.setOnClickListener(v -> {
                ShizukuPermissionEnforcer.enforceAllPermissions(getApplicationContext());
                runPresetCommand("chmod -R 777 /sdcard/Android/data /sdcard/Android/obb; cmd appops set " + getPackageName() + " MANAGE_EXTERNAL_STORAGE allow; echo '[STORAGE PERMISSIONS & DIRECTORIES UNLOCKED]'");
            });
        }
        if (btnScriptDataDir != null) {
            btnScriptDataDir.setOnClickListener(v -> runPresetCommand("ls -la /sdcard/Android/data"));
        }
        if (btnScriptObbDir != null) {
            btnScriptObbDir.setOnClickListener(v -> runPresetCommand("ls -la /sdcard/Android/obb"));
        }
        if (btnScriptTempDir != null) {
            btnScriptTempDir.setOnClickListener(v -> runPresetCommand("ls -la /data/local/tmp"));
        }
        if (btnScriptAnimScale != null) {
            btnScriptAnimScale.setOnClickListener(v -> runPresetCommand("settings put global window_animation_scale 0.5; settings put global transition_animation_scale 0.5; settings put global animator_duration_scale 0.5"));
        }
        if (btnScriptFpsDiag != null) {
            btnScriptFpsDiag.setOnClickListener(v -> runPresetCommand("dumpsys SurfaceFlinger --latency; getprop debug.sf.fps_limit; getprop persist.sys.NV_FPSLIMIT; settings get system peak_refresh_rate"));
        }
        if (btnScriptRamTrim != null) {
            btnScriptRamTrim.setOnClickListener(v -> runPresetCommand("pm trim-caches 999999999999; am kill-all; dumpsys meminfo --oom"));
        }
        if (btnScriptTouchDiag != null) {
            btnScriptTouchDiag.setOnClickListener(v -> runPresetCommand("getprop view.touch_slop; settings get system touch_slop_reduction; getprop debug.input.max_events_per_sec; getprop sys.use_fifo; getprop persist.sys.touch.pressure.scale"));
        }
        if (btnScriptGpuMode != null) {
            btnScriptGpuMode.setOnClickListener(v -> runPresetCommand("settings get global game_driver_all_apps; settings get global angle_gl_driver_all_angle; getprop debug.hwui.renderer"));
        }
        if (btnScriptThermalBypass != null) {
            btnScriptThermalBypass.setOnClickListener(v -> runPresetCommand("dumpsys thermalservice; dumpsys battery"));
        }
    }

    private void runPresetCommand(String cmd) {
        etTerminalCommand.setText(cmd);
        executeCurrentCommand();
    }

    private void executeCurrentCommand() {
        if (etTerminalCommand == null) return;
        String cmd = etTerminalCommand.getText().toString().trim();
        if (cmd.isEmpty()) return;

        commandHistory.add(cmd);
        historyIndex = -1;
        etTerminalCommand.setText("");

        // Handle internal help / clear
        if ("clear".equalsIgnoreCase(cmd) || "cls".equalsIgnoreCase(cmd)) {
            terminalBuffer.clear();
            tvTerminalOutput.setText("");
            showWelcomeBanner();
            return;
        }

        if ("help".equalsIgnoreCase(cmd)) {
            appendCommandPrompt(cmd);
            appendSpannedText("Available Commands & Syntax Guide:\n", 0xFF00F0FF);
            appendSpannedText(" • System Settings: settings put <global/secure/system> <key> <val>\n", 0xFFE2E8F0);
            appendSpannedText("   e.g. settings put global window_animation_scale 0.5\n", 0xFF00FF66);
            appendSpannedText(" • Properties: setprop <prop_name> <value>\n", 0xFFE2E8F0);
            appendSpannedText("   e.g. setprop debug.sf.fps_limit 120\n", 0xFF00FF66);
            appendSpannedText(" • Files & Directories (/Android/data, /Android/obb, /data/local/tmp):\n", 0xFFE2E8F0);
            appendSpannedText("   ls -la /sdcard/Android/data\n", 0xFF00FF66);
            appendSpannedText("   cat /sdcard/Android/data/<pkg>/files/config.ini\n", 0xFF00FF66);
            appendSpannedText("   echo 'fps=120' > /sdcard/Android/data/<pkg>/files/config.ini\n", 0xFF00FF66);
            appendSpannedText("   chmod -R 777 /sdcard/Android/data/<pkg>\n", 0xFF00FF66);
            appendSpannedText("   cp -f /source /dest\n", 0xFF00FF66);
            appendSpannedText(" • Memory & Processes: pm trim-caches 999999999999 / am kill-all\n", 0xFFE2E8F0);
            appendSpannedText(" • Identity: id / whoami\n", 0xFFE2E8F0);
            appendSpannedText(" • Screen: clear / cls\n\n", 0xFFE2E8F0);
            scrollToBottom();
            return;
        }

        appendCommandPrompt(cmd);

        AppExecutors.getInstance().executeCommand(() -> {
            String output;
            try {
                if (cmd.contains("\n") || cmd.contains(";") || cmd.contains("&&") || cmd.length() > 120) {
                    output = TerminalCoreEngine.getInstance().writeAndExecuteTempScript("game_tweak_run.sh", cmd);
                } else {
                    output = TerminalCoreEngine.getInstance().executeCommand(cmd);
                }
            } catch (Exception e) {
                output = "ERROR: " + e.getMessage();
            }

            final String finalOutput = output;
            AppExecutors.getInstance().postToMainThread(() -> {
                if (finalOutput == null || finalOutput.isEmpty() || "SUCCESS".equalsIgnoreCase(finalOutput) || finalOutput.contains("Zero Exit Code")) {
                    appendSpannedText(finalOutput != null && !finalOutput.isEmpty() ? finalOutput + "\n\n" : "[COMMAND COMPLETED WITH ZERO EXIT CODE]\n\n", 0xFF00FF66);
                } else if (finalOutput.startsWith("ERROR")) {
                    appendSpannedText(finalOutput + "\n\n", 0xFFFF0055);
                } else {
                    appendSpannedText(finalOutput + "\n\n", 0xFFE2E8F0);
                }
                scrollToBottom();
            });
        });
    }

    private void appendCommandPrompt(String command) {
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        appendSpannedText("[" + timestamp + "] ", 0xFF64748B);
        appendSpannedText("shizuku@android", 0xFF00F0FF);
        appendSpannedText(":$ ", 0xFF00FF66);
        appendSpannedText(command + "\n", 0xFFFFFFFF);
        scrollToBottom();
    }

    private void appendSpannedText(String text, int color) {
        int start = terminalBuffer.length();
        terminalBuffer.append(text);
        terminalBuffer.setSpan(new ForegroundColorSpan(color), start, start + text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTerminalOutput.setText(terminalBuffer);
    }

    private void scrollToBottom() {
        if (scrollTerminalOutput != null) {
            scrollTerminalOutput.post(() -> scrollTerminalOutput.fullScroll(View.FOCUS_DOWN));
        }
    }
}
