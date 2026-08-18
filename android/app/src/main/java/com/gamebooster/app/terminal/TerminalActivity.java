package com.gamebooster.app.terminal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Pure Cyberpunk Terminal Emulator Activity.
 * Supports direct elevated shell & root execution, script folder management,
 * and high-performance game tweaking commands.
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
    private Button btnTerminalFolder;
    private ImageButton btnTerminalBack;

    private Button btnHistoryPrev;
    private Button btnHistoryNext;

    private Button btnScriptFolderAction;
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

        // Initialize Terminal Folder System
        TerminalFolderManager.getInstance(getApplicationContext()).initTerminalFolder();

        setupWindowInsets();
        initViews();
        setupListeners();
        showWelcomeBanner();
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
        btnTerminalFolder = findViewById(R.id.btn_terminal_folder);
        btnTerminalBack = findViewById(R.id.btn_terminal_back);

        btnHistoryPrev = findViewById(R.id.btn_terminal_history_prev);
        btnHistoryNext = findViewById(R.id.btn_terminal_history_next);

        btnScriptFolderAction = findViewById(R.id.btn_script_folder_action);
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
            tvTerminalStatus.setText("Local Shell (Fallback) • " + androidVer);
            tvTerminalStatus.setTextColor(0xFFFFB800);
        }
    }

    private void showWelcomeBanner() {
        String folderPath = TerminalFolderManager.getInstance(getApplicationContext()).getTerminalDirPath();
        appendSpannedText("====================================================\n", 0xFF00F0FF);
        appendSpannedText("  GAME BOOSTER PRO — PURE CYBER TERMINAL ENGINE\n", 0xFF00FF66);
        appendSpannedText("  Target: Android 13, 14, 15, 16 (API 33-36) Ready\n", 0xFF00F0FF);
        appendSpannedText("  Scripts Folder: " + folderPath + "\n", 0xFFFFB800);
        appendSpannedText("====================================================\n", 0xFF00F0FF);
        appendSpannedText("Commands: help, scripts, run <file>, ls, cd, pwd, cat, clear\n\n", 0xFF94A3B8);
    }

    private void setupListeners() {
        if (btnTerminalBack != null) {
            btnTerminalBack.setOnClickListener(v -> finish());
        }

        if (btnTerminalFolder != null) {
            btnTerminalFolder.setOnClickListener(v -> showTerminalFolderDialog());
        }

        if (btnScriptFolderAction != null) {
            btnScriptFolderAction.setOnClickListener(v -> showTerminalFolderDialog());
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

    public void showTerminalFolderDialog() {
        TerminalFolderManager folderManager = TerminalFolderManager.getInstance(getApplicationContext());
        List<File> files = folderManager.listScriptFiles();

        String[] itemTitles;
        if (files.isEmpty()) {
            itemTitles = new String[]{"➕ [CREATE NEW SCRIPT]"};
        } else {
            itemTitles = new String[files.size() + 1];
            for (int i = 0; i < files.size(); i++) {
                itemTitles[i] = "📜 " + files.get(i).getName();
            }
            itemTitles[files.size()] = "➕ [CREATE NEW SCRIPT]";
        }

        new AlertDialog.Builder(this)
                .setTitle("📁 TERMINAL SCRIPTS FOLDER")
                .setItems(itemTitles, (dialog, which) -> {
                    if (which == itemTitles.length - 1 && (files.isEmpty() || which == files.size())) {
                        showCreateScriptDialog();
                    } else {
                        File selectedFile = files.get(which);
                        showScriptActionDialog(selectedFile);
                    }
                })
                .setNegativeButton("CLOSE", null)
                .show();
    }

    private void showScriptActionDialog(File scriptFile) {
        TerminalFolderManager folderManager = TerminalFolderManager.getInstance(getApplicationContext());
        String[] actions = {"⚡ Execute Script", "📝 View / Edit Script", "🗑️ Delete Script"};

        new AlertDialog.Builder(this)
                .setTitle("📜 " + scriptFile.getName())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        // Execute
                        String cmd = "sh " + scriptFile.getAbsolutePath();
                        etTerminalCommand.setText(cmd);
                        executeCurrentCommand();
                    } else if (which == 1) {
                        // View / Edit
                        showEditScriptDialog(scriptFile);
                    } else if (which == 2) {
                        // Delete
                        folderManager.deleteScript(scriptFile);
                        Toast.makeText(this, "Deleted: " + scriptFile.getName(), Toast.LENGTH_SHORT).show();
                        showTerminalFolderDialog();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void showCreateScriptDialog() {
        TerminalFolderManager folderManager = TerminalFolderManager.getInstance(getApplicationContext());

        View dialogView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null, false);
        EditText etName = new EditText(this);
        etName.setHint("script_name.sh");
        etName.setTextColor(0xFFFFFFFF);
        etName.setHintTextColor(0xFF64748B);

        EditText etContent = new EditText(this);
        etContent.setHint("# Type bash commands here...\necho 'Game Boost Active'\n");
        etContent.setTextColor(0xFF00FF66);
        etContent.setHintTextColor(0xFF64748B);
        etContent.setMinLines(5);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);
        layout.addView(etName);
        layout.addView(etContent);

        new AlertDialog.Builder(this)
                .setTitle("➕ CREATE NEW TERMINAL SCRIPT")
                .setView(layout)
                .setPositiveButton("SAVE & RUN", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String content = etContent.getText().toString();
                    if (name.isEmpty()) name = "custom_script_" + System.currentTimeMillis() + ".sh";
                    folderManager.saveScript(name, content);
                    Toast.makeText(this, "Script saved: " + name, Toast.LENGTH_SHORT).show();
                    etTerminalCommand.setText("run " + name);
                    executeCurrentCommand();
                })
                .setNeutralButton("SAVE ONLY", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String content = etContent.getText().toString();
                    if (name.isEmpty()) name = "custom_script_" + System.currentTimeMillis() + ".sh";
                    folderManager.saveScript(name, content);
                    Toast.makeText(this, "Script saved to terminal folder!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void showEditScriptDialog(File scriptFile) {
        TerminalFolderManager folderManager = TerminalFolderManager.getInstance(getApplicationContext());
        String currentContent = folderManager.readScript(scriptFile);

        EditText etContent = new EditText(this);
        etContent.setText(currentContent);
        etContent.setTextColor(0xFF00FF66);
        etContent.setMinLines(8);
        etContent.setPadding(32, 16, 32, 16);

        new AlertDialog.Builder(this)
                .setTitle("📝 " + scriptFile.getName())
                .setView(etContent)
                .setPositiveButton("SAVE CHANGES", (dialog, which) -> {
                    folderManager.saveScript(scriptFile.getName(), etContent.getText().toString());
                    Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("RUN", (dialog, which) -> {
                    folderManager.saveScript(scriptFile.getName(), etContent.getText().toString());
                    etTerminalCommand.setText("sh " + scriptFile.getAbsolutePath());
                    executeCurrentCommand();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void executeCurrentCommand() {
        if (etTerminalCommand == null) return;
        String cmd = etTerminalCommand.getText().toString().trim();
        if (cmd.isEmpty()) return;

        commandHistory.add(cmd);
        historyIndex = -1;
        etTerminalCommand.setText("");

        // Handle internal clear
        if ("clear".equalsIgnoreCase(cmd) || "cls".equalsIgnoreCase(cmd)) {
            terminalBuffer.clear();
            tvTerminalOutput.setText("");
            showWelcomeBanner();
            return;
        }

        // Handle scripts / folder listing
        if ("scripts".equalsIgnoreCase(cmd) || "folder".equalsIgnoreCase(cmd)) {
            appendCommandPrompt(cmd);
            TerminalFolderManager mgr = TerminalFolderManager.getInstance(getApplicationContext());
            List<File> files = mgr.listScriptFiles();
            appendSpannedText("📁 Terminal Folder: " + mgr.getTerminalDirPath() + "\n", 0xFF00F0FF);
            if (files.isEmpty()) {
                appendSpannedText("  (Folder is empty. Type 'new' or open folder dialog to create scripts)\n\n", 0xFF94A3B8);
            } else {
                for (File f : files) {
                    appendSpannedText("  • " + f.getName() + " (" + f.length() + " bytes)\n", 0xFF00FF66);
                }
                appendSpannedText("Type 'run <filename>' or 'cat <filename>' to execute/view.\n\n", 0xFF94A3B8);
            }
            scrollToBottom();
            return;
        }

        // Handle 'run <script_name>'
        if (cmd.startsWith("run ")) {
            String scriptName = cmd.substring(4).trim();
            TerminalFolderManager mgr = TerminalFolderManager.getInstance(getApplicationContext());
            File scriptFile = new File(mgr.getTerminalDir(), scriptName);
            if (!scriptFile.exists() && !scriptName.endsWith(".sh")) {
                scriptFile = new File(mgr.getTerminalDir(), scriptName + ".sh");
            }
            if (scriptFile.exists()) {
                appendCommandPrompt(cmd);
                appendSpannedText("▶️ Executing Script: " + scriptFile.getName() + "...\n", 0xFF00F0FF);
                final File targetFile = scriptFile;
                AppExecutors.getInstance().executeCommand(() -> {
                    String output = mgr.executeScriptFile(targetFile);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        appendSpannedText(output + "\n\n", 0xFF00FF66);
                        scrollToBottom();
                    });
                });
                return;
            }
        }

        if ("help".equalsIgnoreCase(cmd)) {
            appendCommandPrompt(cmd);
            appendSpannedText("Available Commands & Syntax Guide:\n", 0xFF00F0FF);
            appendSpannedText(" • Terminal Scripts Folder:\n", 0xFF00FF66);
            appendSpannedText("   scripts / folder - List all scripts in terminal folder\n", 0xFFE2E8F0);
            appendSpannedText("   run <script.sh> - Execute script directly from terminal folder\n", 0xFFE2E8F0);
            appendSpannedText(" • Navigation & Files:\n", 0xFF00FF66);
            appendSpannedText("   pwd / ls -la / cd <dir> / cat <file> / mkdir <dir> / rm <file>\n", 0xFFE2E8F0);
            appendSpannedText(" • System Settings & Properties:\n", 0xFF00FF66);
            appendSpannedText("   settings put <global/secure/system> <key> <val>\n", 0xFFE2E8F0);
            appendSpannedText("   setprop <key> <val> / getprop <key>\n", 0xFFE2E8F0);
            appendSpannedText(" • Memory & Identity:\n", 0xFF00FF66);
            appendSpannedText("   pm trim-caches 999999999999 / am kill-all / id / whoami\n", 0xFFE2E8F0);
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
                if (finalOutput == null || finalOutput.isEmpty() || "SUCCESS".equalsIgnoreCase(finalOutput) || finalOutput.contains("Zero Exit Code") || finalOutput.contains("Exit Code 0")) {
                    appendSpannedText(finalOutput != null && !finalOutput.isEmpty() ? finalOutput + "\n\n" : "[COMMAND COMPLETED (Exit Code 0)]\n\n", 0xFF00FF66);
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
        String currentDir = TerminalCoreEngine.getInstance().getCurrentWorkingDir();
        appendSpannedText("[" + timestamp + "] ", 0xFF64748B);
        appendSpannedText("shizuku@android", 0xFF00F0FF);
        appendSpannedText(":" + currentDir + "$ ", 0xFF00FF66);
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
