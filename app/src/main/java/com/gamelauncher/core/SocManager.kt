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
    val architecture: String = "arm64-v8a",
    val coreCount: Int = 8,
    val maxGpuFreqMhz: Int = 0
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

    fun invalidateCache() {
        cachedSocInfo = null
    }

    private fun detectSoc(): SocInfo {
        val hardware = getHardwareFromCpuinfo()
        val socModel = getSocModel()
        val socType = detectSocType(hardware, socModel)
        val (gpuVendor, gpuModel) = detectGpu(socType)
        val isGamingOptimized = isGamingSoc(socType)
        val coreCount = Runtime.getRuntime().availableProcessors()

        return SocInfo(
            socType = socType,
            socName = formatSocName(socType, hardware, socModel),
            socModel = socModel,
            gpuVendor = gpuVendor,
            gpuModel = gpuModel,
            isGamingOptimized = isGamingOptimized,
            supportsBigLittle = detectBigLittle(),
            supportsHyperThreading = detectHyperThreading(),
            architecture = detectArchitecture(),
            coreCount = coreCount,
            maxGpuFreqMhz = detectMaxGpuFreq(gpuVendor)
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
                "/sys/firmware/devicetree/base/model",
                "/proc/device-tree/model",
                "/sys/devices/soc0/machine",
                "/sys/devices/soc0/family",
                "/sys/devices/system/cpu/possible"
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
        } catch (e: Exception) {
            "${Build.MANUFACTURER} ${Build.MODEL}"
        }
    }

    private fun detectSocType(hardware: String, socModel: String): SocType {
        val combined = "$hardware $socModel".lowercase()

        return when {
            combined.contains("snapdragon") || combined.contains("qualcomm") ||
            combined.contains("msm") || combined.contains("qcom") ||
            combined.contains("adreno") -> SocType.SNAPDRAGON

            combined.contains("mt") || combined.contains("mediatek") ||
            combined.contains("helio") || combined.contains("dimensity") ||
            combined.contains("kompanio") -> SocType.MEDIATEK

            combined.contains("exynos") || combined.contains("s5e") ||
            combined.contains("s5p") || combined.contains("samsung") -> SocType.EXYNOS

            combined.contains("kirin") || combined.contains("hi") ||
            combined.contains("huawei") -> SocType.KIRIN

            combined.contains("unisoc") || combined.contains("spreadtrum") ||
            combined.contains("sc9863") || combined.contains("t606") ||
            combined.contains("t610") || combined.contains("t616") ||
            combined.contains("t618") || combined.contains("t820") ||
            combined.contains("t7250") || combined.contains("t760") -> SocType.UNISOC

            combined.contains("tensor") || combined.contains("gs101") ||
            combined.contains("gs201") || combined.contains("gs301") ||
            combined.contains("gs501") || combined.contains("gs601") -> SocType.TENSOR

            combined.contains("rockchip") || combined.contains("rk") -> SocType.UNKNOWN
            combined.contains("allwinner") || combined.contains("sun") -> SocType.UNKNOWN
            combined.contains("amlogic") -> SocType.UNKNOWN
            combined.contains("bcm") || combined.contains("broadcom") -> SocType.UNKNOWN
            combined.contains("ingenic") || combined.contains("jz") -> SocType.UNKNOWN

            else -> SocType.UNKNOWN
        }
    }

    private fun detectGpu(socType: SocType): Pair<GpuVendor, String> {
        val gpuPaths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpu_model",
            "/sys/class/kgsl/kgsl-3d0/model",
            "/sys/kernel/gpu/gpu_model",
            "/sys/module/pvrsrvkm/parameters/gpu_id",
            "/sys/devices/platform/gpu.0/devfreq/gpu.0/available_frequencies",
            "/sys/devices/platform/3d00.mali/devfreq/3d00.mali/governor",
            "/sys/devices/platform/13000000.mali/devfreq/13000000.mali/governor"
        )
        for (path in gpuPaths) {
            val gpuInfo = runCatching { File(path).readText().trim() }.getOrNull()
            if (!gpuInfo.isNullOrBlank()) {
                val gpuLower = gpuInfo.lowercase()
                return when {
                    gpuLower.contains("adreno") -> GpuVendor.ADRENO to gpuInfo
                    gpuLower.contains("mali") -> GpuVendor.MALI to gpuInfo
                    gpuLower.contains("immortalis") -> GpuVendor.IMMORTAL to gpuInfo
                    gpuLower.contains("powervr") || gpuLower.contains("sgx") -> GpuVendor.POWERVR to gpuInfo
                    else -> GpuVendor.UNKNOWN to gpuInfo
                }
            }
        }
        return when (socType) {
            SocType.SNAPDRAGON -> GpuVendor.ADRENO to "Adreno GPU"
            SocType.MEDIATEK -> {
                val model = getSocModel().uppercase()
                if (model.contains("DIMENSITY") && (model.contains("9000") || model.contains("8000") || model.contains("9400") || model.contains("9300")))
                    GpuVendor.IMMORTAL to "Immortalis-GPU"
                else
                    GpuVendor.MALI to "Mali GPU"
            }
            SocType.EXYNOS -> GpuVendor.MALI to "Mali GPU"
            SocType.KIRIN -> GpuVendor.MALI to "Mali GPU"
            SocType.UNISOC -> GpuVendor.MALI to "Mali GPU"
            SocType.TENSOR -> GpuVendor.MALI to "Mali GPU"
            SocType.UNKNOWN -> GpuVendor.UNKNOWN to "Unknown GPU"
        }
    }

    private fun detectMaxGpuFreq(gpuVendor: GpuVendor): Int {
        val paths = when (gpuVendor) {
            GpuVendor.ADRENO -> listOf(
                "/sys/class/kgsl/kgsl-3d0/max_gpuclk",
                "/sys/class/kgsl/kgsl-3d0/gpuclk"
            )
            GpuVendor.MALI, GpuVendor.IMMORTAL -> listOf(
                "/sys/devices/platform/mali.0/devfreq/mali.0/max_freq",
                "/sys/kernel/gpu/gpu_max_clock"
            )
            else -> emptyList()
        }
        return try {
            paths.firstNotNullOfOrNull { path ->
                runCatching { File(path).readText().trim().toIntOrNull() }.getOrNull()
            }?.let { freq -> if (freq > 1_000_000) freq / 1_000_000 else freq } ?: 0
        } catch (_: Exception) { 0 }
    }

    private fun isGamingSoc(socType: SocType): Boolean {
        return when (socType) {
            SocType.SNAPDRAGON -> {
                val model = getSocModel().uppercase()
                listOf("8", "7", "6", "4").any { model.contains(it) }
            }
            SocType.MEDIATEK -> {
                val model = getSocModel().uppercase()
                model.contains("G") || model.contains("DIMENSITY")
            }
            SocType.EXYNOS -> true
            SocType.KIRIN -> true
            SocType.UNISOC -> getSocModel().uppercase().let { it.contains("T8") || it.contains("T7") || it.contains("T6") }
            SocType.TENSOR -> true
            SocType.UNKNOWN -> false
        }
    }

    private fun detectBigLittle(): Boolean {
        return try {
            val coreDirs = File("/sys/devices/system/cpu").listFiles { f ->
                f.name.startsWith("cpu") && f.name.drop(3).all { it.isDigit() }
            } ?: return false
            coreDirs.count { dir ->
                runCatching {
                    File(dir, "cpufreq/scaling_min_freq").readText().trim().isNotBlank()
                }.getOrDefault(false)
            } > 4
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
        return if (Build.SUPPORTED_ABIS.contains("arm64-v8a")) "arm64-v8a"
        else if (Build.SUPPORTED_ABIS.contains("armeabi-v7a")) "armeabi-v7a"
        else Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
    }

    private fun formatSocName(socType: SocType, hardware: String, model: String): String {
        return when (socType) {
            SocType.SNAPDRAGON -> {
                val m = model.lowercase()
                when {
                    m.contains("8 elite gen 2") || m.contains("sm8900") -> "Snapdragon 8 Elite Gen 2"
                    m.contains("8 elite") || m.contains("sm8750") -> "Snapdragon 8 Elite"
                    m.contains("8 gen 5") || m.contains("sm8850") -> "Snapdragon 8 Gen 5"
                    m.contains("8 gen 4") || m.contains("sm8750") -> "Snapdragon 8 Gen 4"
                    m.contains("8 gen 3") || m.contains("sm8650") -> "Snapdragon 8 Gen 3"
                    m.contains("8 gen 2") || m.contains("sm8550") -> "Snapdragon 8 Gen 2"
                    m.contains("8 gen 1") || m.contains("sm8450") -> "Snapdragon 8 Gen 1"
                    m.contains("888") || m.contains("sm8350") -> "Snapdragon 888"
                    m.contains("870") || m.contains("sm8250") -> "Snapdragon 870"
                    m.contains("865") || m.contains("sm8250") -> "Snapdragon 865"
                    m.contains("7+ gen 3") || m.contains("sm7675") -> "Snapdragon 7+ Gen 3"
                    m.contains("7 gen 3") || m.contains("sm7550") -> "Snapdragon 7 Gen 3"
                    m.contains("7s gen 2") || m.contains("sm7435") -> "Snapdragon 7s Gen 2"
                    m.contains("7+ gen 2") || m.contains("sm7475") -> "Snapdragon 7+ Gen 2"
                    m.contains("7 gen 1") || m.contains("sm7450") -> "Snapdragon 7 Gen 1"
                    m.contains("6 gen 4") || m.contains("sm6650") -> "Snapdragon 6 Gen 4"
                    m.contains("6 gen 3") || m.contains("sm6475") -> "Snapdragon 6 Gen 3"
                    m.contains("6 gen 1") || m.contains("sm6450") -> "Snapdragon 6 Gen 1"
                    m.contains("4 gen 2") || m.contains("sm4450") -> "Snapdragon 4 Gen 2"
                    m.contains("4 gen 1") || m.contains("sm4375") -> "Snapdragon 4 Gen 1"
                    else -> "Snapdragon (${hardware.take(20)})"
                }
            }
            SocType.MEDIATEK -> {
                val m = model.uppercase()
                when {
                    m.contains("9500") || m.contains("MT9500") -> "Dimensity 9500"
                    m.contains("9400") || m.contains("MT9400") -> "Dimensity 9400"
                    m.contains("9400+") || m.contains("MT9400P") -> "Dimensity 9400+"
                    m.contains("9300") || m.contains("MT9300") -> "Dimensity 9300"
                    m.contains("9300+") || m.contains("MT9300P") -> "Dimensity 9300+"
                    m.contains("9200") || m.contains("MT9200") -> "Dimensity 9200"
                    m.contains("9200+") || m.contains("MT9200P") -> "Dimensity 9200+"
                    m.contains("9000") || m.contains("MT9000") -> "Dimensity 9000"
                    m.contains("8400") || m.contains("MT8400") -> "Dimensity 8400"
                    m.contains("8300") || m.contains("MT8300") -> "Dimensity 8300"
                    m.contains("8300+") || m.contains("MT8300P") -> "Dimensity 8300+"
                    m.contains("8250") || m.contains("MT8250") -> "Dimensity 8250"
                    m.contains("8200") || m.contains("MT8200") -> "Dimensity 8200"
                    m.contains("8100") || m.contains("MT8100") -> "Dimensity 8100"
                    m.contains("8025") || m.contains("MT8025") -> "Dimensity 8025"
                    m.contains("7400") || m.contains("MT7400") -> "Dimensity 7400"
                    m.contains("7350") || m.contains("MT7350") -> "Dimensity 7350"
                    m.contains("7300") || m.contains("MT7300") -> "Dimensity 7300"
                    m.contains("7250") || m.contains("MT7250") -> "Dimensity 7250"
                    m.contains("7200") || m.contains("MT7200") -> "Dimensity 7200"
                    m.contains("7050") || m.contains("MT7050") -> "Dimensity 7050"
                    m.contains("7030") || m.contains("MT7030") -> "Dimensity 7030"
                    m.contains("G100") -> "Helio G100"
                    m.contains("G99") -> "Helio G99"
                    m.contains("G96") -> "Helio G96"
                    m.contains("G95") -> "Helio G95"
                    m.contains("G91") -> "Helio G91"
                    m.contains("G88") -> "Helio G88"
                    m.contains("G85") -> "Helio G85"
                    m.contains("G84") -> "Helio G84"
                    m.contains("G80") -> "Helio G80"
                    m.contains("G70") -> "Helio G70"
                    m.contains("G36") -> "Helio G36"
                    m.contains("G35") -> "Helio G35"
                    m.contains("G25") -> "Helio G25"
                    else -> "MediaTek $model"
                }
            }
            SocType.EXYNOS -> {
                val m = model.uppercase()
                when {
                    m.contains("2600") -> "Exynos 2600"
                    m.contains("2500") -> "Exynos 2500"
                    m.contains("2400") -> "Exynos 2400"
                    m.contains("2200") -> "Exynos 2200"
                    m.contains("2100") -> "Exynos 2100"
                    m.contains("1580") -> "Exynos 1580"
                    m.contains("1480") -> "Exynos 1480"
                    m.contains("1380") -> "Exynos 1380"
                    m.contains("1280") -> "Exynos 1280"
                    m.contains("1080") -> "Exynos 1080"
                    m.contains("W1000") || m.contains("W920") -> "Exynos W"
                    else -> "Exynos ${model.take(10)}"
                }
            }
            SocType.KIRIN -> {
                val m = model.uppercase()
                when {
                    m.contains("9010") -> "Kirin 9010"
                    m.contains("9000S") || m.contains("9000s") -> "Kirin 9000S"
                    m.contains("9000") -> "Kirin 9000"
                    m.contains("8000") -> "Kirin 8000"
                    m.contains("990") -> "Kirin 990"
                    else -> "Kirin ${model.take(10)}"
                }
            }
            SocType.UNISOC -> {
                val m = model.uppercase()
                when {
                    m.contains("T820") || m.contains("T770") -> "Unisoc T820/T770"
                    m.contains("T765") || m.contains("T760") -> "Unisoc T760/T765"
                    m.contains("T7250") -> "Unisoc T7250"
                    m.contains("T620") || m.contains("T618") || m.contains("T616") -> "Unisoc T6xx"
                    m.contains("T610") || m.contains("T606") -> "Unisoc T6xx"
                    else -> "Unisoc ${model.take(10)}"
                }
            }
            SocType.TENSOR -> {
                val m = model.uppercase()
                when {
                    m.contains("G5") || m.contains("GS601") -> "Google Tensor G5"
                    m.contains("G4") || m.contains("GS501") -> "Google Tensor G4"
                    m.contains("G3") || m.contains("GS301") -> "Google Tensor G3"
                    m.contains("G2") || m.contains("GS201") -> "Google Tensor G2"
                    m.contains("G1") || m.contains("GS101") -> "Google Tensor G1"
                    else -> "Google Tensor ${model.take(6)}"
                }
            }
            SocType.UNKNOWN -> {
                val fallback = "${Build.MANUFACTURER} ${Build.MODEL}".take(30)
                hardware.take(25).ifEmpty { fallback.ifEmpty { "Unknown SoC" } }
            }
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
            "Cache" to "echo 1 > /sys/kernel/debug/sched_energy_aware",
            "LLCC BW" to "echo 100 > /sys/class/devfreq/soc:qcom,cpu-llcc-bw/max_freq",
            "DDR BW" to "echo 100 > /sys/class/devfreq/soc:qcom,cpubw/max_freq"
        )
    }

    private fun getMediaTekOptimizations(): List<Pair<String, String>> {
        return listOf(
            "HyperEngine" to "echo 1 > /sys/module/mtk_vcore_debug/parameters/enable",
            "Game Mode" to "echo 1 > /sys/devices/system/cpu/cpu0/cpufreq/game_mode",
            "GPU Performance" to "echo 'performance' > /sys/class/misc/mtk-vpu/devfreq/mtk-vpu/governor",
            "CPU Boost" to "echo 1 > /sys/kernel/ged/boost_gpu_enable"
        )
    }

    private fun getExynosOptimizations(): List<Pair<String, String>> {
        return listOf(
            "Game Optimizer" to "echo 1 > /sys/class/kgsl/kgsl-3d0/gpu_governor",
            "CPU Boost" to "echo performance > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
            "GPU Boost" to "echo 1 > /sys/devices/platform/gpu.0/devfreq/gpu.0/boost"
        )
    }

    private fun getKirinOptimizations(): List<Pair<String, String>> {
        return listOf(
            "GPU Turbo" to "echo 1 > /sys/class/dss/display/turbo",
            "CPU Turbo" to "echo performance > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
            "NPU Boost" to "echo 1 > /sys/kernel/hisi/npu/boost"
        )
    }

    private fun getUnisocOptimizations(): List<Pair<String, String>> {
        return listOf(
            "CPU Governor" to "echo performance > /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor",
            "I/O Optimization" to "echo noop > /sys/block/mmcblk0/queue/scheduler",
            "GPU Boost" to "echo 1 > /sys/class/misc/mali0/device/power_policy"
        )
    }

    private fun getTensorOptimizations(): List<Pair<String, String>> {
        return listOf(
            "Tensor Gaming Mode" to "echo performance > /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor",
            "Mali GPU Boost" to "echo performance > /sys/class/devfreq/*mali*/governor",
            "TPU Boost" to "echo 1 > /sys/devices/platform/vertex.0/boost"
        )
    }

    private fun getGenericOptimizations(): List<Pair<String, String>> {
        return listOf(
            "CPU Governor" to "echo performance > /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor",
            "I/O Deadline" to "echo deadline > /sys/block/mmcblk0/queue/scheduler"
        )
    }

    fun isSocSupported(): Boolean = getSocInfo().socType != SocType.UNKNOWN

    fun getDeviceRating(): Int {
        val socInfo = getSocInfo()
        return when (socInfo.socType) {
            SocType.SNAPDRAGON -> {
                val model = getSocModel().lowercase()
                when {
                    model.contains("8 elite gen 2") -> 10
                    model.contains("8 elite") -> 10
                    model.contains("8 gen 5") -> 10
                    model.contains("8 gen 4") || model.contains("8 gen 3") -> 10
                    model.contains("8 gen 2") -> 9
                    model.contains("8 gen 1") || model.contains("888") -> 9
                    model.contains("870") || model.contains("865") -> 8
                    model.contains("7+ gen 3") || model.contains("7+ gen 2") -> 8
                    model.contains("7 gen 3") || model.contains("7s gen 2") -> 7
                    model.contains("7 gen 1") -> 7
                    model.contains("855") || model.contains("845") -> 7
                    model.contains("6 gen 4") || model.contains("6 gen 3") -> 7
                    model.contains("6 gen 1") -> 6
                    model.contains("4 gen 2") || model.contains("4 gen 1") -> 5
                    else -> 6
                }
            }
            SocType.MEDIATEK -> {
                val model = getSocModel().lowercase()
                when {
                    model.contains("9500") || model.contains("9400") || model.contains("9300") -> 10
                    model.contains("9200") || model.contains("9000") -> 9
                    model.contains("8400") || model.contains("8300") || model.contains("8250") -> 8
                    model.contains("8200") || model.contains("8100") -> 8
                    model.contains("8025") || model.contains("7400") || model.contains("7350") || model.contains("7300") -> 7
                    model.contains("7200") || model.contains("7050") -> 6
                    model.contains("g100") || model.contains("g99") || model.contains("g96") -> 7
                    model.contains("g95") || model.contains("g90") -> 6
                    model.contains("g88") || model.contains("g85") || model.contains("g84") || model.contains("g80") -> 5
                    model.contains("g70") || model.contains("g36") || model.contains("g35") || model.contains("g25") -> 4
                    else -> 5
                }
            }
            SocType.EXYNOS -> {
                val model = getSocModel().lowercase()
                when {
                    model.contains("2600") || model.contains("2500") -> 10
                    model.contains("2400") -> 9
                    model.contains("2200") || model.contains("2100") -> 8
                    model.contains("1580") || model.contains("1480") || model.contains("1380") -> 7
                    model.contains("1280") -> 6
                    else -> 6
                }
            }
            SocType.KIRIN -> {
                val model = getSocModel().lowercase()
                when {
                    model.contains("9010") || model.contains("9000s") -> 9
                    model.contains("9000") -> 8
                    model.contains("990") || model.contains("8000") -> 7
                    else -> 6
                }
            }
            SocType.UNISOC -> {
                val model = getSocModel().uppercase()
                when {
                    model.contains("T820") || model.contains("T770") -> 7
                    model.contains("T765") || model.contains("T760") || model.contains("T7250") -> 6
                    model.contains("T618") || model.contains("T616") || model.contains("T620") -> 6
                    model.contains("T610") || model.contains("T606") -> 5
                    else -> 4
                }
            }
            SocType.TENSOR -> {
                val model = getSocModel().uppercase()
                when {
                    model.contains("G6") || model.contains("GS801") -> 10
                    model.contains("G5") || model.contains("GS601") -> 9
                    model.contains("G4") || model.contains("GS501") -> 9
                    model.contains("G3") || model.contains("GS301") -> 9
                    model.contains("G2") || model.contains("GS201") -> 8
                    model.contains("G1") || model.contains("GS101") -> 7
                    else -> 7
                }
            }
            SocType.UNKNOWN -> 4
        }
    }
}
