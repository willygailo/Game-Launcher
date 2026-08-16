/**
 * Game Booster Pro — Core App & UI Utilities
 */

document.addEventListener("DOMContentLoaded", () => {
  if (window.lucide) {
    lucide.createIcons();
  }
});

function showToast(message) {
  const toast = document.getElementById("toast");
  const toastMsg = document.getElementById("toast-msg");
  if (toast && toastMsg) {
    toastMsg.textContent = message;
    toast.classList.remove("translate-y-20", "opacity-0");
    toast.classList.add("translate-y-0", "opacity-100");
    setTimeout(() => {
      toast.classList.remove("translate-y-0", "opacity-100");
      toast.classList.add("translate-y-20", "opacity-0");
    }, 2600);
  } else {
    console.log("[TOAST]", message);
  }
}

// Global modal helpers
function openModal(modalId) {
  const el = document.getElementById(modalId);
  if (el) {
    el.classList.remove("hidden");
    el.classList.add("flex");
  }
}

function closeModal(modalId) {
  const el = document.getElementById(modalId);
  if (el) {
    el.classList.add("hidden");
    el.classList.remove("flex");
  }
}
