/**
 * Game Booster Pro — Shizuku ADB Terminal & Console Logger
 */

function logShell(cmd) {
  const shellLog = document.getElementById("shell-log");
  if (!shellLog) return;
  const p = document.createElement("p");
  p.className = "text-slate-300 font-mono text-[11px] leading-relaxed";
  p.innerHTML = `<span class="text-cyan-400 font-bold">$ shizuku@android:~#</span> ${cmd}`;
  shellLog.appendChild(p);
  shellLog.scrollTop = shellLog.scrollHeight;

  // Bridge call to Android native if running in WebView
  if (window.AndroidBridge && typeof window.AndroidBridge.executeShizukuCmd === "function") {
    try {
      const result = window.AndroidBridge.executeShizukuCmd(cmd);
      const resP = document.createElement("p");
      resP.className = result.startsWith("ERROR") ? "text-red-400 text-[10px]" : "text-emerald-400 text-[10px]";
      resP.textContent = `[ADB OUTPUT] ${result}`;
      shellLog.appendChild(resP);
      shellLog.scrollTop = shellLog.scrollHeight;
    } catch (err) {
      console.error("Bridge Error:", err);
    }
  }
}

function executeUserCmd() {
  const shellInput = document.getElementById("shell-cmd-input");
  if (!shellInput) return;
  const cmd = shellInput.value.trim();
  if (cmd) {
    logShell(cmd);
    shellInput.value = "";
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const runShellBtn = document.getElementById("run-shell-btn");
  const shellInput = document.getElementById("shell-cmd-input");
  const clearLogBtn = document.getElementById("clear-log-btn");

  if (runShellBtn) runShellBtn.addEventListener("click", executeUserCmd);
  if (shellInput) {
    shellInput.addEventListener("keypress", (e) => {
      if (e.key === "Enter") executeUserCmd();
    });
  }

  if (clearLogBtn) {
    clearLogBtn.addEventListener("click", () => {
      const shellLog = document.getElementById("shell-log");
      if (shellLog) {
        shellLog.innerHTML = `<p class="text-slate-500">[INFO] Shizuku Console cleared.</p>`;
      }
    });
  }
});
