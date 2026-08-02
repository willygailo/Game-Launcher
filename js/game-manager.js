/**
 * Game Booster Pro — Game Library & Tuning Manager
 */

let activeTuningGameCard = null;
let activeEngineMode = "3d";
let activeTuneHz = "120";

function bindGameItemListeners(card) {
  const tuneBtn = card.querySelector(".tune-game-btn");
  const launchBtn = card.querySelector(".launch-game-btn");
  const tuneModal = document.getElementById("tune-game-modal");

  if (tuneBtn && tuneModal) {
    tuneBtn.addEventListener("click", () => {
      activeTuningGameCard = card;
      const title = card.getAttribute("data-game-title");
      const engine = card.getAttribute("data-engine") || "3d";
      const fps = card.getAttribute("data-fps") || "120";
      const cpu = card.getAttribute("data-cpu") || "extreme";

      document.getElementById("tune-game-title").textContent = title;

      const btn3d = document.getElementById("tune-engine-3d");
      const btn2d = document.getElementById("tune-engine-2d");
      if (engine === "3d") {
        btn3d.classList.add("bg-cyan-500", "text-slate-950");
        btn3d.classList.remove("glass", "text-slate-300");
        btn2d.classList.remove("bg-cyan-500", "text-slate-950");
        btn2d.classList.add("glass", "text-slate-300");
        activeEngineMode = "3d";
      } else {
        btn2d.classList.add("bg-cyan-500", "text-slate-950");
        btn2d.classList.remove("glass", "text-slate-300");
        btn3d.classList.remove("bg-cyan-500", "text-slate-950");
        btn3d.classList.add("glass", "text-slate-300");
        activeEngineMode = "2d";
      }

      document.querySelectorAll(".tune-hz-btn").forEach(btn => {
        if (btn.getAttribute("data-hz") === fps) {
          btn.classList.add("bg-cyan-500", "text-slate-950");
          btn.classList.remove("glass", "text-slate-300");
        } else {
          btn.classList.remove("bg-cyan-500", "text-slate-950");
          btn.classList.add("glass", "text-slate-300");
        }
      });
      activeTuneHz = fps;

      document.getElementById("tune-cpu-mode").value = cpu;

      tuneModal.classList.remove("hidden");
      tuneModal.classList.add("flex");
    });
  }

  if (launchBtn) {
    launchBtn.addEventListener("click", () => {
      const title = card.getAttribute("data-game-title");
      const engine = card.getAttribute("data-engine") || "3d";
      const fps = card.getAttribute("data-fps") || "120";
      const cpu = card.getAttribute("data-cpu") || "extreme";
      const touch = card.getAttribute("data-touch") || "480";
      const pkgName = card.getAttribute("data-pkg") || "com.game.app";

      showToast(`🚀 Launching ${title} (${engine.toUpperCase()} • ${fps}Hz • CPU ${cpu.toUpperCase()})...`);
      logShell(`cmd game mode performance ${pkgName}`);
      logShell(`cmd device_config put game_overlay ${pkgName} mode=2,fps=${fps === 'MAX' ? 240 : fps}`);
      logShell(`setprop debug.graphics.game_default_frame_rate.disabled 1`);
      logShell(`setprop debug.hwui.renderer ${engine === "3d" ? "vulkan" : "skia"}`);

      if (window.AndroidBridge && typeof window.AndroidBridge.setTargetFps === "function") {
        window.AndroidBridge.setTargetFps(parseInt(fps) || 120);
      }
    });
  }
}

document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".game-item").forEach(card => bindGameItemListeners(card));

  const tuneModal = document.getElementById("tune-game-modal");
  const closeTuneBtn = document.getElementById("close-tune-game");
  const saveTuneBtn = document.getElementById("save-tune-game");

  if (closeTuneBtn && tuneModal) {
    closeTuneBtn.addEventListener("click", () => {
      tuneModal.classList.add("hidden");
      tuneModal.classList.remove("flex");
    });
  }

  if (saveTuneBtn && tuneModal) {
    saveTuneBtn.addEventListener("click", () => {
      if (activeTuningGameCard) {
        const title = activeTuningGameCard.getAttribute("data-game-title");
        const cpu = document.getElementById("tune-cpu-mode").value;

        activeTuningGameCard.setAttribute("data-engine", activeEngineMode);
        activeTuningGameCard.setAttribute("data-fps", activeTuneHz);
        activeTuningGameCard.setAttribute("data-cpu", cpu);

        const badgeText = activeTuningGameCard.querySelector(".game-badge-text");
        if (badgeText) {
          badgeText.textContent = `${activeEngineMode === '3d' ? '3D Vulkan' : '2D Pixel'} Engine • ${activeTuneHz}Hz • CPU ${cpu.toUpperCase()}`;
        }

        showToast(`✅ Saved ${title} Config: ${activeEngineMode.toUpperCase()} Engine, ${activeTuneHz}Hz`);
        logShell(`Saved Config for ${title}: Engine=${activeEngineMode}, Hz=${activeTuneHz}, CPU=${cpu}`);
      }

      tuneModal.classList.add("hidden");
      tuneModal.classList.remove("flex");
    });
  }
});
