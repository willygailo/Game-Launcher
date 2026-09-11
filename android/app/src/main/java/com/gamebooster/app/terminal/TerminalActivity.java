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
 * True interactive Android shell emulator supporting ANSI escape colors,
 * persistent directory state, tab autocompletion, Termux-style modifier bar,
 * and high-performance kernel / game tuning script execution.
 */
public class TerminalActivity extends AppCompatActivity {

    private View terminalRootLayout;
    private TextView tvTerminalStatus;
    private TextView tvTerminalOutput;
    private ScrollView scrollTerminalOutput;
    private TextView tvTerminalPromptPrefix;
    private EditText etTerminalCommand;
    private Button btnTerminalRun;
    private Button btnClearTerminal;
    private Button btnCopyTerminal;
    private Button btnTerminalFolder;
    private ImageButton btnTerminalBack;

    // Quick Modifier Toolbar Buttons (Termux-style)
    private Button btnKeyEsc;
    private Button btnKeyTab;
    private Button btnKeyCtrlC;
    private Button btnKeyCtrlL;
    private Button btnKeyArrowUp;
    private Button btnKeyArrowDown;
    private Button btnKeyArrowLeft;
    private Button btnKeyArrowRight;
    private Button btnKeyPgUp;
    private Button btnKeyPgDn;
    private Button btnKeySlash;
    private Button btnKeyDash;
    private Button btnKeyTilde;
    private Button btnKeyPipe;
    private Button btnKeyRedirect;
    private Button btnKeyDollar;
    private Button btnKeyAmp;
    private Button btnKeySemicolon;
    private Button btnKeyRoot;
    private Button btnKeyTmp;
    private Button btnKeySdcard;

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
        tvTerminalPromptPrefix = findViewById(R.id.tv_terminal_prompt_prefix);
        etTerminalCommand = findViewById(R.id.et_terminal_command);
        btnTerminalRun = findViewById(R.id.btn_terminal_run);
        btnClearTerminal = findViewById(R.id.btn_clear_terminal);
        btnCopyTerminal = findViewById(R.id.btn_copy_terminal);
        btnTerminalFolder = findViewById(R.id.btn_terminal_folder);
        btnTerminalBack = findViewById(R.id.btn_terminal_back);

        // Modifier Toolbar (Termux Extra Keys)
        btnKeyEsc = findViewById(R.id.btn_key_esc);
        btnKeyTab = findViewById(R.id.btn_key_tab);
        btnKeyCtrlC = findViewById(R.id.btn_key_ctrl_c);
        btnKeyCtrlL = findViewById(R.id.btn_key_ctrl_l);
        btnKeyArrowUp = findViewById(R.id.btn_key_arrow_up);
        btnKeyArrowDown = findViewById(R.id.btn_key_arrow_down);
        btnKeyArrowLeft = findViewById(R.id.btn_key_arrow_left);
        btnKeyArrowRight = findViewById(R.id.btn_key_arrow_right);
        btnKeyPgUp = findViewById(R.id.btn_key_pgup);
        btnKeyPgDn = findViewById(R.id.btn_key_pgdn);
        btnKeySlash = findViewById(R.id.btn_key_slash);
        btnKeyDash = findViewById(R.id.btn_key_dash);
        btnKeyTilde = findViewById(R.id.btn_key_tilde);
        btnKeyPipe = findViewById(R.id.btn_key_pipe);
        btnKeyRedirect = findViewById(R.id.btn_key_redirect);
        btnKeyDollar = findViewById(R.id.btn_key_dollar);
        btnKeyAmp = findViewById(R.id.btn_key_amp);
        btnKeySemicolon = findViewById(R.id.btn_key_semicolon);
        btnKeyRoot = findViewById(R.id.btn_key_root);
        btnKeyTmp = findViewById(R.id.btn_key_tmp);
        btnKeySdcard = findViewById(R.id.btn_key_sdcard);

        updateStatusBanner();
        updatePromptPrefix();
    }

    private void updateStatusBanner() {
        boolean hasShizuku = ShizukuExecutor.hasShizukuPermission();
        String user = TerminalCoreEngine.getInstance().getPromptUserPrefix();
        String pwd = TerminalCoreEngine.getInstance().getCurrentWorkingDir();
        String displayDir = pwd.replace("/storage/emulated/0", "/sdcard");
        if (hasShizuku) {
            tvTerminalStatus.setText(user + ":" + displayDir + " $ [PTY Shell]");
            tvTerminalStatus.setTextColor(0xFF4ADE80);
        } else {
            tvTerminalStatus.setText(user + ":" + displayDir + " $ [Standard Shell]");
            tvTerminalStatus.setTextColor(0xFFFFB800);
        }
    }

    private void updatePromptPrefix() {
        if (tvTerminalPromptPrefix != null) {
            String sym = TerminalCoreEngine.getInstance().getPromptSymbol();
            tvTerminalPromptPrefix.setText(">" + sym);
        }
    }

    private void showWelcomeBanner() {
        String folderPath = TerminalFolderManager.getInstance(getApplicationContext()).getTerminalDirPath();
        boolean hasShizuku = ShizukuExecutor.hasShizukuPermission();
        String user = TerminalCoreEngine.getInstance().getPromptUserPrefix();
        String currentDir = TerminalCoreEngine.getInstance().getCurrentWorkingDir();

        appendSpannedText("Welcome to Termux (Shizuku Privileged Shell)!\n\n", 0xFF4ADE80);
        appendSpannedText("Current Path:    " + currentDir + " (Internal Storage)\n", 0xFF38BDF8);
        appendSpannedText("Scripts Folder:  " + folderPath + "\n\n", 0xFFFFD700);
        appendSpannedText("Common Commands:\n", 0xFF38BDF8);
        appendSpannedText(" • ls [-la]              List files & directories with color & details\n", 0xFF94A3B8);
        appendSpannedText(" • cd <folder>           Navigate to folder (e.g. cd Download, cd ..)\n", 0xFF94A3B8);
        appendSpannedText(" • pkg list / search     Query installed game packages\n", 0xFF94A3B8);
        appendSpannedText(" • neofetch              System hardware & Android info\n", 0xFF94A3B8);
        appendSpannedText(" • scripts               Open terminal script manager\n\n", 0xFF94A3B8);
        appendSpannedText("Shell Status: " + (hasShizuku ? "UID 2000 (Shell) • Elevated Shizuku Active" : "Local App Shell") + "\n", 0xFF00FF66);
        appendSpannedText("Type 'help' for built-ins, TAB for autocompletion, CTRL+C to cancel.\n\n", 0xFF64748B);
    }

    private void setupListeners() {
        if (btnTerminalBack != null) {
            btnTerminalBack.setOnClickListener(v -> finish());
        }

        if (btnTerminalFolder != null) {
            btnTerminalFolder.setOnClickListener(v -> showTerminalFolderDialog());
        }

        if (btnClearTerminal != null) {
            btnClearTerminal.setOnClickListener(v -> clearTerminalBuffer());
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

        // History Navigation & Arrow Keys
        View.OnClickListener historyPrevAction = v -> {
            if (commandHistory.isEmpty()) return;
            if (historyIndex == -1) {
                historyIndex = commandHistory.size() - 1;
            } else if (historyIndex > 0) {
                historyIndex--;
            }
            etTerminalCommand.setText(commandHistory.get(historyIndex));
            etTerminalCommand.setSelection(etTerminalCommand.getText().length());
        };

        View.OnClickListener historyNextAction = v -> {
            if (commandHistory.isEmpty() || historyIndex == -1) return;
            if (historyIndex < commandHistory.size() - 1) {
                historyIndex++;
                etTerminalCommand.setText(commandHistory.get(historyIndex));
                etTerminalCommand.setSelection(etTerminalCommand.getText().length());
            } else {
                historyIndex = -1;
                etTerminalCommand.setText("");
            }
        };

        if (btnKeyArrowUp != null) btnKeyArrowUp.setOnClickListener(historyPrevAction);
        if (btnKeyArrowDown != null) btnKeyArrowDown.setOnClickListener(historyNextAction);

        if (btnKeyArrowLeft != null) {
            btnKeyArrowLeft.setOnClickListener(v -> {
                if (etTerminalCommand == null) return;
                int pos = etTerminalCommand.getSelectionStart();
                if (pos > 0) {
                    etTerminalCommand.setSelection(pos - 1);
                }
            });
        }

        if (btnKeyArrowRight != null) {
            btnKeyArrowRight.setOnClickListener(v -> {
                if (etTerminalCommand == null) return;
                int pos = etTerminalCommand.getSelectionStart();
                if (pos < etTerminalCommand.getText().length()) {
                    etTerminalCommand.setSelection(pos + 1);
                }
            });
        }

        if (btnKeyPgUp != null) {
            btnKeyPgUp.setOnClickListener(v -> {
                if (scrollTerminalOutput != null) {
                    scrollTerminalOutput.pageScroll(View.FOCUS_UP);
                }
            });
        }

        if (btnKeyPgDn != null) {
            btnKeyPgDn.setOnClickListener(v -> {
                if (scrollTerminalOutput != null) {
                    scrollTerminalOutput.pageScroll(View.FOCUS_DOWN);
                }
            });
        }

        // Modifier Toolbar Handlers
        if (btnKeyEsc != null) {
            btnKeyEsc.setOnClickListener(v -> {
                if (etTerminalCommand != null) {
                    etTerminalCommand.setText("");
                }
            });
        }
        if (btnKeyTab != null) {
            btnKeyTab.setOnClickListener(v -> handleTabCompletion());
        }
        if (btnKeyCtrlC != null) {
            btnKeyCtrlC.setOnClickListener(v -> {
                TerminalCoreEngine.getInstance().cancelRunningCommand();
                appendSpannedText("^C\n", 0xFFFF3366);
                scrollToBottom();
            });
        }
        if (btnKeyCtrlL != null) {
            btnKeyCtrlL.setOnClickListener(v -> clearTerminalBuffer());
        }
        if (btnKeySlash != null) btnKeySlash.setOnClickListener(v -> insertSymbolAtCursor("/"));
        if (btnKeyDash != null) btnKeyDash.setOnClickListener(v -> insertSymbolAtCursor("-"));
        if (btnKeyTilde != null) btnKeyTilde.setOnClickListener(v -> insertSymbolAtCursor("~"));
        if (btnKeyPipe != null) btnKeyPipe.setOnClickListener(v -> insertSymbolAtCursor("|"));
        if (btnKeyRedirect != null) btnKeyRedirect.setOnClickListener(v -> insertSymbolAtCursor(">"));
        if (btnKeyDollar != null) btnKeyDollar.setOnClickListener(v -> insertSymbolAtCursor("$"));
        if (btnKeyAmp != null) btnKeyAmp.setOnClickListener(v -> insertSymbolAtCursor("&"));
        if (btnKeySemicolon != null) btnKeySemicolon.setOnClickListener(v -> insertSymbolAtCursor(";"));
        if (btnKeyRoot != null) {
            btnKeyRoot.setOnClickListener(v -> {
                if (etTerminalCommand != null) {
                    etTerminalCommand.setText("cd /");
                    executeCurrentCommand();
                }
            });
        }
        if (btnKeyTmp != null) {
            btnKeyTmp.setOnClickListener(v -> {
                if (etTerminalCommand != null) {
                    etTerminalCommand.setText("cd /data/local/tmp");
                    executeCurrentCommand();
                }
            });
        }
        if (btnKeySdcard != null) {
            btnKeySdcard.setOnClickListener(v -> {
                if (etTerminalCommand != null) {
                    etTerminalCommand.setText("cd /sdcard");
                    executeCurrentCommand();
                }
            });
        }
    }

    private void clearTerminalBuffer() {
        terminalBuffer.clear();
        tvTerminalOutput.setText("");
        showWelcomeBanner();
    }

    private void insertSymbolAtCursor(String symbol) {
        if (etTerminalCommand == null) return;
        int start = Math.max(etTerminalCommand.getSelectionStart(), 0);
        int end = Math.max(etTerminalCommand.getSelectionEnd(), 0);
        etTerminalCommand.getText().replace(Math.min(start, end), Math.max(start, end), symbol, 0, symbol.length());
    }

    private void handleTabCompletion() {
        if (etTerminalCommand == null) return;
        String currentText = etTerminalCommand.getText().toString();
        List<String> completions = TerminalCoreEngine.getInstance().getCompletions(currentText);

        if (completions.isEmpty()) {
            Toast.makeText(this, "No completions found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (completions.size() == 1) {
            String match = completions.get(0);
            int lastSpace = currentText.lastIndexOf(' ');
            String newText;
            if (lastSpace >= 0) {
                newText = currentText.substring(0, lastSpace + 1) + match;
            } else {
                newText = match;
            }
            etTerminalCommand.setText(newText);
            etTerminalCommand.setSelection(newText.length());
        } else {
            // Display all matching completion candidates
            appendCommandPrompt(currentText);
            StringBuilder sb = new StringBuilder("\u001B[1;36mPossible completions:\u001B[0m\n");
            for (String c : completions) {
                sb.append("  \u001B[32m").append(c).append("\u001B[0m\n");
            }
            appendAnsiText(sb.toString() + "\n");
            scrollToBottom();
        }
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
                        String cmd = "sh " + scriptFile.getAbsolutePath();
                        etTerminalCommand.setText(cmd);
                        executeCurrentCommand();
                    } else if (which == 1) {
                        showEditScriptDialog(scriptFile);
                    } else if (which == 2) {
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

        EditText etName = new EditText(this);
        etName.setHint("script_name.sh");
        etName.setTextColor(0xFFFFFFFF);
        etName.setHintTextColor(0xFF64748B);

        EditText etContent = new EditText(this);
        etContent.setHint("#!/system/bin/sh\n# Type bash commands here...\necho 'Game Boost Active'\n");
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
                    etTerminalCommand.setText("sh " + folderManager.getTerminalDirPath() + "/" + name);
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

        // Handle internal exit
        if ("exit".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) {
            finish();
            return;
        }

        // Handle internal clear
        if ("clear".equalsIgnoreCase(cmd) || "cls".equalsIgnoreCase(cmd)) {
            clearTerminalBuffer();
            return;
        }

        // Handle scripts / folder listing
        if ("scripts".equalsIgnoreCase(cmd) || "folder".equalsIgnoreCase(cmd)) {
            appendCommandPrompt(cmd);
            TerminalFolderManager mgr = TerminalFolderManager.getInstance(getApplicationContext());
            List<File> files = mgr.listScriptFiles();
            appendSpannedText("📁 Terminal Folder: " + mgr.getTerminalDirPath() + "\n", 0xFF00F0FF);
            if (files.isEmpty()) {
                appendSpannedText("  (Folder is empty. Open folder dialog to create scripts)\n\n", 0xFF94A3B8);
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
                        appendAnsiText(output + "\n\n");
                        updatePromptPrefix();
                        scrollToBottom();
                    });
                });
                return;
            }
        }

        appendCommandPrompt(cmd);

        AppExecutors.getInstance().executeCommand(() -> {
            TerminalCoreEngine.TerminalResult result;
            try {
                if (cmd.contains("\n") || cmd.length() > 200) {
                    String tempOut = TerminalCoreEngine.getInstance().writeAndExecuteTempScript("game_tweak_run.sh", cmd);
                    result = new TerminalCoreEngine.TerminalResult(tempOut, 0, TerminalCoreEngine.getInstance().getCurrentWorkingDir());
                } else {
                    result = TerminalCoreEngine.getInstance().executeCommand(cmd);
                }
            } catch (Exception e) {
                result = new TerminalCoreEngine.TerminalResult("ERROR: " + e.getMessage(), 1, TerminalCoreEngine.getInstance().getCurrentWorkingDir());
            }

            final TerminalCoreEngine.TerminalResult finalRes = result;
            AppExecutors.getInstance().postToMainThread(() -> {
                String outText = finalRes.output;
                if (outText == null || outText.isEmpty()) {
                    if (finalRes.exitCode != 0) {
                        appendSpannedText("[Exit Code " + finalRes.exitCode + "]\n\n", 0xFFFF3366);
                    }
                } else {
                    appendAnsiText(outText + "\n");
                    if (finalRes.exitCode != 0 && !outText.contains("Exit Code")) {
                        appendSpannedText("[Exit Code " + finalRes.exitCode + "]\n\n", 0xFFFF3366);
                    } else {
                        appendSpannedText("\n", 0xFF00FF66);
                    }
                }
                updatePromptPrefix();
                updateStatusBanner();
                scrollToBottom();
            });
        });
    }

    private void appendCommandPrompt(String command) {
        String userPrefix = TerminalCoreEngine.getInstance().getPromptUserPrefix();
        String currentDir = TerminalCoreEngine.getInstance().getCurrentWorkingDir();
        String displayDir = currentDir.replace("/storage/emulated/0", "/sdcard");
        String symbol = TerminalCoreEngine.getInstance().getPromptSymbol();

        appendSpannedText(userPrefix + " ", 0xFF4ADE80); // Termux Light Green
        appendSpannedText(displayDir, 0xFF38BDF8);       // Termux Cyan Path
        appendSpannedText(" " + symbol + " ", 0xFFF1F5F9); // White prompt
        appendSpannedText(command + "\n", 0xFFFFFFFF);
        scrollToBottom();
    }

    private void appendAnsiText(String text) {
        SpannableStringBuilder parsed = AnsiColorParser.parseAnsi(text, 0xFFE2E8F0);
        terminalBuffer.append(parsed);
        // Keep buffer bounded
        if (terminalBuffer.length() > 80000) {
            terminalBuffer.delete(0, 20000);
        }
        tvTerminalOutput.setText(terminalBuffer);
    }

    private void appendSpannedText(String text, int color) {
        int start = terminalBuffer.length();
        terminalBuffer.append(text);
        terminalBuffer.setSpan(new ForegroundColorSpan(color), start, start + text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Keep buffer bounded
        if (terminalBuffer.length() > 80000) {
            terminalBuffer.delete(0, 20000);
        }
        tvTerminalOutput.setText(terminalBuffer);
    }

    private void scrollToBottom() {
        if (scrollTerminalOutput != null) {
            scrollTerminalOutput.post(() -> scrollTerminalOutput.fullScroll(View.FOCUS_DOWN));
        }
    }
}
