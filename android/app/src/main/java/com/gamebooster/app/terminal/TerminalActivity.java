package com.gamebooster.app.terminal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
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
 * Features:
 * - Built-in SetEdit Engine (System, Secure, Global Tables)
 * - Direct Storage Script Runner (/storage/emulated/0, Download, SAF Picker)
 * - Multi-tier Execution: Elevated Shizuku / temporary Root UID 2000 & zero-root fallback
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

    // Quick Action Chips
    private Button btnScriptLoadFile;
    private Button btnScriptSetEditGuide;
    private Button btnScriptSetEditTweaks;
    private Button btnScriptSetpropGpu;
    private Button btnScriptWhoami;
    private Button btnScriptFixStorage;
    private Button btnScriptDataDir;
    private Button btnScriptObbDir;
    private Button btnScriptTempDir;
    private Button btnScriptSettingsAll;
    private Button btnScriptAnimScale;
    private Button btnScriptFpsDiag;
    private Button btnScriptRamTrim;
    private Button btnScriptTouchDiag;
    private Button btnScriptGpuMode;
    private Button btnScriptThermalBypass;
    private Button btnScriptPingDiag;

    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;
    private final SpannableStringBuilder terminalBuffer = new SpannableStringBuilder();

    private ActivityResultLauncher<Intent> scriptPickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable Edge-to-Edge display support for Android 13, 14, 15, 16
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_terminal);

        initScriptPickerLauncher();
        setupWindowInsets();
        initViews();
        setupListeners();
        showWelcomeBanner();
    }

    private void initScriptPickerLauncher() {
        scriptPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            executeSelectedScriptUri(uri);
                        }
                    }
                }
        );
    }

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

        btnScriptLoadFile = findViewById(R.id.btn_script_load_file);
        btnScriptSetEditGuide = findViewById(R.id.btn_script_setedit_guide);
        btnScriptSetEditTweaks = findViewById(R.id.btn_script_setedit_tweaks);
        btnScriptSetpropGpu = findViewById(R.id.btn_script_setprop_gpu);
        btnScriptWhoami = findViewById(R.id.btn_script_whoami);
        btnScriptFixStorage = findViewById(R.id.btn_script_fix_storage);
        btnScriptDataDir = findViewById(R.id.btn_script_data_dir);
        btnScriptObbDir = findViewById(R.id.btn_script_obb_dir);
        btnScriptTempDir = findViewById(R.id.btn_script_temp_dir);
        btnScriptSettingsAll = findViewById(R.id.btn_script_settings_all);
        btnScriptAnimScale = findViewById(R.id.btn_script_anim_scale);
        btnScriptFpsDiag = findViewById(R.id.btn_script_fps_diag);
        btnScriptRamTrim = findViewById(R.id.btn_script_ram_trim);
        btnScriptTouchDiag = findViewById(R.id.btn_script_touch_diag);
        btnScriptGpuMode = findViewById(R.id.btn_script_gpu_mode);
        btnScriptThermalBypass = findViewById(R.id.btn_script_thermal_bypass);
        btnScriptPingDiag = findViewById(R.id.btn_script_ping_diag);

        updateStatusBanner();
    }

    private void updateStatusBanner() {
        boolean hasShizuku = ShizukuExecutor.hasShizukuPermission();
        String androidVer = "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
        if (hasShizuku) {
            tvTerminalStatus.setText("🟢 ROOT/ADB PRIVILEGED (UID 2000) • " + androidVer);
            tvTerminalStatus.setTextColor(0xFF00FF66);
        } else {
            tvTerminalStatus.setText("🟡 NATIVE SYSTEM RUNTIME (STANDARD SHELL) • " + androidVer);
            tvTerminalStatus.setTextColor(0xFFFFB800);
        }
    }

    private void showWelcomeBanner() {
        appendSpannedText("⚡ WG-RVS CYBER TERMINAL ENGINE v2.0\n", 0xFF00F0FF);

        boolean hasShizuku = ShizukuExecutor.hasShizukuPermission();
        if (hasShizuku) {
            appendSpannedText("🟢 Privilege: Elevated Shell / Temporary Root (UID 2000)\n", 0xFF00FF66);
        } else {
            appendSpannedText("🟡 Privilege: Native Linux Runtime Engine (Zero-Root)\n", 0xFFFFB800);
        }

        appendSpannedText("💡 Tip: Type 'help' or 'setedit' for guide, or tap quick-action chips above.\n", 0xFF94A3B8);
        appendSpannedText("📂 Run scripts: Type 'sh /storage/emulated/0/Download/name.sh' or tap '📂 Run .sh File'.\n\n", 0xFF64748B);
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
        if (btnScriptLoadFile != null) {
            btnScriptLoadFile.setOnClickListener(v -> openScriptFilePicker());
        }
        if (btnScriptSetEditGuide != null) {
            btnScriptSetEditGuide.setOnClickListener(v -> runPresetCommand("setedit help"));
        }
        if (btnScriptSetEditTweaks != null) {
            btnScriptSetEditTweaks.setOnClickListener(v -> runPresetCommand("settings put system peak_refresh_rate 120; settings put system min_refresh_rate 120; settings put global window_animation_scale 0.5; settings put global transition_animation_scale 0.5; settings put global animator_duration_scale 0.5; settings put global game_driver_all_apps 1; settings put system touch_slop_reduction 1; echo '[SETEDIT GAMING PACK APPLIED]'"));
        }
        if (btnScriptSetpropGpu != null) {
            btnScriptSetpropGpu.setOnClickListener(v -> runPresetCommand("setprop debug.egl.hw 1; setprop debug.sf.hw 1; setprop debug.hwui.renderer skiagl; setprop renderthread.initialize.priority 1; getprop debug.egl.hw; echo '[GPU & HW PROPERTIES CONFIGURED]'"));
        }
        if (btnScriptWhoami != null) {
            btnScriptWhoami.setOnClickListener(v -> runPresetCommand("id; whoami; uname -a; getprop ro.build.version.release"));
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
        if (btnScriptSettingsAll != null) {
            btnScriptSettingsAll.setOnClickListener(v -> runPresetCommand("settings get system peak_refresh_rate; settings get global window_animation_scale; settings get global game_driver_all_apps"));
        }
        if (btnScriptAnimScale != null) {
            btnScriptAnimScale.setOnClickListener(v -> runPresetCommand("settings put global window_animation_scale 0.5; settings put global transition_animation_scale 0.5; settings put global animator_duration_scale 0.5; echo '0.5x UI Speed Applied'"));
        }
        if (btnScriptFpsDiag != null) {
            btnScriptFpsDiag.setOnClickListener(v -> runPresetCommand("dumpsys SurfaceFlinger --latency; getprop debug.sf.fps_limit; getprop persist.sys.NV_FPSLIMIT; settings get system peak_refresh_rate"));
        }
        if (btnScriptRamTrim != null) {
            btnScriptRamTrim.setOnClickListener(v -> runPresetCommand("pm trim-caches 999999999999; am kill-all; dumpsys meminfo --oom; free -m"));
        }
        if (btnScriptTouchDiag != null) {
            btnScriptTouchDiag.setOnClickListener(v -> runPresetCommand("getprop view.touch_slop; settings get system touch_slop_reduction; getprop debug.input.max_events_per_sec; getprop sys.use_fifo"));
        }
        if (btnScriptGpuMode != null) {
            btnScriptGpuMode.setOnClickListener(v -> runPresetCommand("settings get global game_driver_all_apps; settings get global angle_gl_driver_all_angle; getprop debug.hwui.renderer"));
        }
        if (btnScriptThermalBypass != null) {
            btnScriptThermalBypass.setOnClickListener(v -> runPresetCommand("dumpsys thermalservice; dumpsys battery; cat /sys/class/thermal/thermal_zone0/temp 2>/dev/null"));
        }
        if (btnScriptPingDiag != null) {
            btnScriptPingDiag.setOnClickListener(v -> runPresetCommand("getprop net.dns1; ping -c 3 1.1.1.1"));
        }
    }

    private void openScriptFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] mimeTypes = {"text/plain", "text/x-sh", "application/x-sh", "application/octet-stream", "*/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            scriptPickerLauncher.launch(intent);
        } catch (Exception e) {
            try {
                Intent fallback = new Intent(Intent.ACTION_GET_CONTENT);
                fallback.setType("*/*");
                scriptPickerLauncher.launch(fallback);
            } catch (Exception ex) {
                Toast.makeText(this, "Could not open file picker: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void executeSelectedScriptUri(Uri uri) {
        String fileName = "script.sh";
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
                cursor.close();
            }
        } catch (Throwable ignored) {}

        final String scriptName = fileName;
        appendCommandPrompt("run " + scriptName + " (from Storage)");
        Toast.makeText(this, "Executing script: " + scriptName, Toast.LENGTH_SHORT).show();

        AppExecutors.getInstance().executeCommand(() -> {
            String output = TerminalCoreEngine.getInstance().executeScriptFromUri(this, uri, scriptName);
            AppExecutors.getInstance().postToMainThread(() -> {
                appendSpannedText(output + "\n\n", 0xFF00FF66);
                scrollToBottom();
            });
        });
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
            appendSpannedText("Available Commands & Syntax Guide (Universal):\n", 0xFF00F0FF);
            appendSpannedText(" • Built-in SetEdit Engine (System, Secure, Global Tables):\n", 0xFFE2E8F0);
            appendSpannedText("   setedit put system peak_refresh_rate 120\n", 0xFF00FF66);
            appendSpannedText("   setedit put global window_animation_scale 0.5\n", 0xFF00FF66);
            appendSpannedText("   setedit put global game_driver_all_apps 1\n", 0xFF00FF66);
            appendSpannedText("   setedit list system / setedit search refresh\n", 0xFF00FF66);
            appendSpannedText(" • Storage Script Execution (/storage/emulated/0, /sdcard, Downloads):\n", 0xFFE2E8F0);
            appendSpannedText("   sh /storage/emulated/0/Download/game_boost.sh\n", 0xFF00FF66);
            appendSpannedText("   run tweak.sh  (Auto-locates in Download, deploys to /data/local/tmp & runs)\n", 0xFF00FF66);
            appendSpannedText("   Tap '📂 Run .sh File' button to pick any script file from storage\n", 0xFF94A3B8);
            appendSpannedText(" • System Properties (setprop / getprop):\n", 0xFFE2E8F0);
            appendSpannedText("   setprop debug.egl.hw 1 / setprop debug.sf.hw 1\n", 0xFF00FF66);
            appendSpannedText("   getprop ro.build.version.release / getprop ro.product.model\n", 0xFF00FF66);
            appendSpannedText(" • Device Config (Scheduler & Game Mode):\n", 0xFFE2E8F0);
            appendSpannedText("   device_config put game_overlay com.gamebooster.app mode=2\n", 0xFF00FF66);
            appendSpannedText(" • Files & Directories (/Android/data, /Android/obb, /data/local/tmp):\n", 0xFFE2E8F0);
            appendSpannedText("   ls -la /sdcard/Android/data\n", 0xFF00FF66);
            appendSpannedText("   cat /sdcard/Android/data/<pkg>/files/config.ini\n", 0xFF00FF66);
            appendSpannedText(" • Diagnostics & RAM:\n", 0xFFE2E8F0);
            appendSpannedText("   pm trim-caches 999999999999 / dumpsys SurfaceFlinger --latency\n", 0xFF00FF66);
            appendSpannedText(" • Screen Control: clear / cls\n\n", 0xFFE2E8F0);
            scrollToBottom();
            return;
        }

        appendCommandPrompt(cmd);

        AppExecutors.getInstance().executeCommand(() -> {
            String output;
            boolean isShizuku = TerminalCoreEngine.getInstance().isPrivilegedRootActive();
            try {
                if (cmd.contains("\n") || (cmd.contains(";") && !cmd.startsWith("setedit")) || cmd.contains("&&") || cmd.length() > 140) {
                    output = TerminalCoreEngine.getInstance().writeAndExecuteTempScript(this, "game_tweak_run.sh", cmd);
                } else {
                    output = TerminalCoreEngine.getInstance().executeCommand(this, cmd);
                }
            } catch (Exception e) {
                output = "ERROR: " + e.getMessage();
            }

            final String finalOutput = output;
            final boolean finalShizuku = isShizuku;
            AppExecutors.getInstance().postToMainThread(() -> {
                String tag = finalShizuku ? "[SHIZUKU/ROOT PRIVILEGED]" : "[SYSTEM RUNTIME]";
                if (finalOutput == null || finalOutput.isEmpty() || "SUCCESS".equalsIgnoreCase(finalOutput) || finalOutput.contains("Exit Code 0")) {
                    appendSpannedText(tag + " " + (finalOutput != null && !finalOutput.isEmpty() ? finalOutput : "SUCCESS (Zero Exit Code)") + "\n\n", 0xFF00FF66);
                } else if (finalOutput.startsWith("ERROR") || finalOutput.contains("Permission Denial") || finalOutput.contains("Permission denied")) {
                    appendSpannedText(tag + " " + finalOutput + "\n", 0xFFFF0055);
                    if (!finalShizuku) {
                        appendSpannedText("💡 Connect Shizuku to grant Temporary Root (UID 2000) for privileged access.\n\n", 0xFFFFB800);
                    } else {
                        appendSpannedText("\n", 0xFFFF0055);
                    }
                } else {
                    appendSpannedText(finalOutput + "\n\n", 0xFFE2E8F0);
                }
                scrollToBottom();
            });
        });
    }

    private void appendCommandPrompt(String command) {
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        boolean isShizuku = TerminalCoreEngine.getInstance().isPrivilegedRootActive();

        appendSpannedText("┌──(", 0xFF00F0FF);
        if (isShizuku) {
            appendSpannedText("root㉿matrix", 0xFF00FF66);
        } else {
            appendSpannedText("user㉿system", 0xFFFFB800);
        }
        appendSpannedText(")-[~]\n", 0xFF00F0FF);

        appendSpannedText("└─" + (isShizuku ? "# " : "$ "), isShizuku ? 0xFF00FF66 : 0xFFFFB800);
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
