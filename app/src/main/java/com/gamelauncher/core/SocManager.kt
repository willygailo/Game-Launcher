package com.gamelauncher.core

import android.os.Build
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

enum class SocType {
    SNAPDRAGON,
    MEDIATEK,
    EXYNOS,
    KIRIN,
    UNISOC,
    TENSOR,
    UNKNOWN
}

enum class GpuVendor {
    ADRENO,
    MALI,
    POWERVR,
    IMMORTAL,
    UNKNOWN
}

data class SocInfo(
    val socType: SocType = SocType.UNKNOWN,
    val socName: String = "Unknown",
    val socModel: String = "",
    val gpuVendor: GpuVendor = GpuVendor.UNKNOWN,
    val gpuModel: String = "",
    val isGamingOptimized: Boolean = false,
    val supportsHyperThreading: Boolean = false,
    val supportsBigLittle: Boolean = false,
    val architecture: String = "arm64-v8a"
)

@Singleton
class SocManager @Inject constructor() {

    private var cachedSocInfo: SocInfo? = null

    fun getSocInfo(): SocInfo {
        cachedSocInfo?.let { return it }

        val socInfo = detectSoc()
        cachedSocInfo = socInfo
        return socInfo
    }

    private fun detectSoc(): SocInfo {
        val hardware = getHardwareFromCpuinfo()
        val socModel = getSocModel()
        val socType = detectSocType(hardware, socModel)
        val (gpuVendor, gpuModel) = detectGpu(socType)
        val isGamingOptimized = isGamingSoc(socType)

        return SocInfo(
            socType = socType,
            socName = formatSocName(socType, hardware, socModel),
            socModel = socModel,
            gpuVendor = gpuVendor,
            gpuModel = gpuModel,
            isGamingOptimized = isGamingOptimized,
            supportsBigLittle = detectBigLittle(),
            supportsHyperThreading = detectHyperThreading(),
            architecture = detectArchitecture()
        )
    }

    private fun getHardwareFromCpuinfo(): String {
        return try {
            File("/proc/cpuinfo").bufferedReader().use { reader ->
                reader.lineSequence()
                    .firstOrNull { it.startsWith("Hardware", true) || it.startsWith("model name", true) }
                    ?.substringAfter(":")?.trim() ?: getBuildHardware()
            }
        } catch (e: Exception) { getBuildHardware() }
    }

    private fun getBuildHardware(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.HARDWARE
        } else {
            @Suppress("DEPRECATION")
            Build.HARDWARE
        }
    }

    private fun getSocModel(): String {
        return try {
            val socPaths = listOf(
                "/sys/class/soc/soc_name",
                "/sys/devices/soc0/soc_id",
                "/sys/devices/system/soc/soc0/id",
                "/sys/firmware/devicetree/base/model"
            )
            for (path in socPaths) {
                val content = runCatching { File(path).readText().trim() }.getOrNull()
                if (!content.isNullOrBlank() && content.length > 2) {
                    return content
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MODEL.takeIf { it.isNotBlank() } ?: getBuildHardware()
            } else {
                getBuildHardware()
            }
        } catch (e: Exception) { getBuildHardware() }
    }

    private fun detectSocType(hardware: String, socModel: String): SocType {
        val combined = "$hardware $socModel".lowercase()
        
        return when {
            combined.contains("snapdragon") || combined.contains("sd") || 
            combined.contains("qualcomm") || combined.contains("msm") ||
            combined.contains("sm") -> SocType.SNAPDRAGON
            
            combined.contains("mt") || combined.contains("mediatek") ||
            combined.contains("helio") || combined.contains("dimensity") -> SocType.MEDIATEK
             
            combined.contains("exynos") || combined.contains("samsung") ||
            combined.contains("s5e") || combined.contains("s5p") -> SocType.EXYNOS
             
            combined.contains("kirin") || combined.contains("hi") ||
            combined.contains("huawei") || combined.contains("honor") -> SocType.KIRIN

            combined.contains("unisoc") || combined.contains("spreadtrum") ||
            combined.contains("sc9863") || combined.contains("t606") ||
            combined.contains("t610") || combined.contains("t616") ||
            combined.contains("t618") || combined.contains("t820") -> SocType.UNISOC

            combined.contains("tensor") || combined.contains("gs101") ||
            combined.contains("gs201") || combined.contains("gs301") -> SocType.TENSOR
            
            else -> SocType.UNKNOWN
        }
    }

    private fun detectGpu(socType: SocType): Pair<GpuVendor, String> {
        val gpuPaths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpu_model",
            "/sys/class/kgsl/kgsl-3d0/model",
            "/sys/kernel/gpu/gpu_model",
            "/sys/module/pvrsrvkm/parameters/gpu_id"
        )

        for (path in gpuPaths) {
            val gpuInfo = runCatching { File(path).readText().trim() }.getOrNull()
            if (!gpuInfo.isNullOrBlank()) {
                val gpuLower = gpuInfo.lowercase()
                return when {
                    gpuLower.contains("adreno") -> GpuVendor.ADRENO to gpuInfo
                    gpuLower.contains("mali") -> GpuVendor.MALI to gpuInfo
                    gpuLower.contains("powervr") || gpuLower.contains("sgx") -> GpuVendor.POWERVR to gpuInfo
                    gpuLower.contains("immortal") -> GpuVendor.IMMORTAL to gpuInfo
                    else -> GpuVendor.UNKNOWN to gpuInfo
                }
            }
        }

        return when (socType) {
            SocType.SNAPDRAGON -> GpuVendor.ADRENO to "Adreno GPU"
            SocType.MEDIATEK -> GpuVendor.MALI to "Mali GPU"
            SocType.EXYNOS -> GpuVendor.MALI to "Mali GPU"
            SocType.KIRIN -> GpuVendor.MALI to "Mali GPU"
            SocType.UNISOC -> GpuVendor.MALI to "Mali GPU"
            SocType.TENSOR -> GpuVendor.MALI to "Mali GPU"
            SocType.UNKNOWN -> GpuVendor.UNKNOWN to "Unknown GPU"
        }
    }

    private fun isGamingSoc(socType: SocType): Boolean {
        return when (socType) {
            SocType.SNAPDRAGON -> {
                val model = getSocModel().uppercase()
                val gamingSeries = listOf("8", "7", "6")
                gamingSeries.any { model.contains(it) }
            }
            SocType.MEDIATEK -> {
                val model = getSocModel().uppercase()
                model.contains("G") || model.contains("DIMENSITY")
            }
            SocType.EXYNOS -> true
            SocType.KIRIN -> true
            SocType.UNISOC -> {
                val model = getSocModel().uppercase()
                model.contains("T8") || model.contains("T7") || model.contains("T6")
            }
            SocType.TENSOR -> true
            SocType.UNKNOWN -> false
        }
    }

    private fun detectBigLittle(): Boolean {
        return try {
            val coreDirs = File("/sys/devices/system/cpu").listFiles { f -> 
                f.name.startsWith("cpu") && f.name.drop(3).all { it.isDigit() }
            } ?: return false
            
            val coreCounts = coreDirs.count { dir ->
                runCatching { 
                    File(dir, "cpufreq/scaling_min_freq").readText().trim().isNotBlank() 
                }.getOrDefault(false)
            }
            coreCounts > 4
        } catch (e: Exception) { false }
    }

    private fun detectHyperThreading(): Boolean {
        return try {
            val path = "/sys/devices/system/cpu/cpu0/topology/thread_siblings_list"
            val siblings = runCatching { File(path).readText().trim() }.getOrNull() ?: return false
            siblings.contains(",")
        } catch (e: Exception) { false }
    }

    private fun detectArchitecture(): String {
        return if (Build.SUPPORTED_ABIS.contains("arm64-v8a")) {
            "arm64-v8a"
        } else if (Build.SUPPORTED_ABIS.contains("armeabi-v7a")) {
            "armeabi-v7a"
        } else {
            Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        }
    }

    private fun formatSocName(socType: SocType, hardware: String, model: String): String {
        return when (socType) {
            SocType.SNAPDRAGON -> {
                val sdPattern = Regex("(\\d{3,4})")
                val match = sdPattern.find(model)
                if (match != null) {
                    "Snapdragon ${match.value.take(2).drop(1)} Gen ${match.value.takeLast(2)}"
                } else {
                    "Snapdragon (${hardware.take(20)})"
                }
            }
            SocType.MEDIATEK -> {
                if (model.contains("Dimensity")) "Dimensity Series"
                else if (model.contains("Helio")) "Helio ${model.substringAfter("Helio")}"
                else "MediaTek ${model.take(15)}"
            }
            SocType.EXYNOS -> "Exynos ${model.take(10)}"
            SocType.KIRIN -> "Kirin ${model.take(10)}"
            SocType.UNISOC -> "Unisoc ${model.take(10)}"
            SocType.TENSOR -> "Google Tensor ${model.take(6)}"
            SocType.UNKNOWN -> hardware.take(25).ifEmpty { "Unknown SoC" }
        }
    }

    fun getGamingOptimizationCommands(): List<Pair<String, String>> {
        val socInfo = getSocInfo()
        return when (socInfo.socType) {
            SocType.SNAPDRAGON -> getSnapdragonOptimizations()
            SocType.MEDIATEK -> getMediaTekOptimizations()
            SocType.EXYNOS -> getExynosOptimizations()
            SocType.KIRIN -> getKirinOptimizations()
            SocType.UNISOC -> getUnisocOptimizations()
            SocType.TENSOR -> getTensorOptimizations()
            SocType.UNKNOWN -> getGenericOptimizations()
        }
    }

    private fun getSnapdragonOptimizations(): List<Pair<String, String>> {
        return listOf(
            "Gaming Mode" to "echo 'high_performance' > /sys/class/devfreq/soc:qcom,cpu-llcc-bw/governor",
            "GPU Boost" to "echo 'performance' > /sys/class/kgsl/kgsl-3d0/devfreq/governor",
            "AI Engine" to "echo 1 > /sys/devices/system/cpu/cpu0/cpufreq/boost",
            "Cache" to "echo 1 > /sys/kernel/debug/sched_energy_aware"
        )
    }

    private fun getMediaTekOptimizations(): List<Pair<String, String>> {
        return listOf(
            "HyperEngine" to "echo 1 > /sys/module/mtk_vcore_debug/parameters/enable",
            "Game Mode" to "echo 1 > /sys/devices/system/cpu/cpu0/cpufreq/game_mode",
            "GPU Performance" to "echo 'performance' > /sys/class/misc/mtk-vpu/devfreq/mtk-vpu/governor"
        )
    }

    private fun getExynosOptimizations(): List<Pair<String, String>> {
        return listOf(
            "Game Optimizer" to "echo 1 > /sys/class/kgsl/kgsl-3d0/gpu_governor",
            "CPU Boost" to "echo performance > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
        )
    }

    private fun getKirinOptimizations(): List<Pair<String, String>> {
        return listOf(
            "GPU Turbo" to "echo 1 > /sys/class/dss/display/turbo",
            "CPU Turbo" to "echo performance > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
        )
    }

    private fun getUnisocOptimizations(): List<Pair<String, String>> {
        return listOf(
            "CPU Governor" to "echo performance > /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor",
            "I/O Optimization" to "echo noop > /sys/block/mmcblk0/queue/scheduler"
        )
    }

    private fun getTensorOptimizations(): List<Pair<String, String>> {
        return listOf(
            "Tensor Gaming Mode" to "echo performance > /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor",
            "Mali GPU Boost" to "echo performance > /sys/class/devfreq/*mali*/governor"
        )
    }

    private fun getGenericOptimizations(): List<Pair<String, String>> {
        return listOf(
            "CPU Governor" to "echo performance > /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor"
        )
    }

    fun isSocSupported(): Boolean = getSocInfo().socType != SocType.UNKNOWN

    fun getDeviceRating(): Int {
        val socInfo = getSocInfo()
        return when (socInfo.socType) {
            SocType.SNAPDRAGON -> {
                val model = getSocModel()
                when {
                    model.contains("8 Gen 3") || model.contains("8 Gen 2") -> 10
                    model.contains("8 Gen 1") || model.contains("888") -> 9
                    model.contains("870") || model.contains("865") -> 8
                    model.contains("855") -> 7
                    else -> 6
                }
            }
            SocType.MEDIATEK -> {
                val model = getSocModel()
                when {
                    model.contains("9000") || model.contains("8000") -> 9
                    model.contains("1200") || model.contains("1100") -> 8
                    else -> 6
                }
            }
            SocType.EXYNOS -> 7
            SocType.KIRIN -> 7
            SocType.UNISOC -> {
                val model = getSocModel().uppercase()
                when {
                    model.contains("T820") || model.contains("T770") -> 7
                    model.contains("T618") || model.contains("T616") -> 6
                    else -> 5
                }
            }
            SocType.TENSOR -> {
                val model = getSocModel().uppercase()
                when {
                    model.contains("G3") -> 9
                    model.contains("G2") -> 8
                    model.contains("G1") -> 7
                    else -> 7
                }
            }
            SocType.UNKNOWN -> 5
        }
    }
}
