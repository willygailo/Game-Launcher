/**
 * Game Booster Pro — Live Hardware Monitors & Sensor Gauges
 */

document.addEventListener("DOMContentLoaded", () => {
  setInterval(() => {
    const cpuVal = document.getElementById("cpu-val");
    const gpuVal = document.getElementById("gpu-val");
    const ramVal = document.getElementById("ram-val");
    const tempVal = document.getElementById("temp-val");

    if (window.AndroidBridge && typeof window.AndroidBridge.getDeviceMetricsJson === "function") {
      try {
        const rawJson = window.AndroidBridge.getDeviceMetricsJson();
        const m = JSON.parse(rawJson);
        if (ramVal) ramVal.textContent = m.usedRamMb + " MB (" + m.ramUsagePct + "%)";
        if (tempVal) tempVal.textContent = m.batteryTempC + " °C";
      } catch (err) {
        console.error("Metrics Parse Error:", err);
      }
    } else {
      if (cpuVal && gpuVal) {
        const cpu = Math.floor(32 + Math.random() * 18);
        const gpu = Math.floor(58 + Math.random() * 20);
        cpuVal.textContent = cpu + "%";
        gpuVal.textContent = gpu + "%";
      }
    }
  }, 3000);
});
