package com.gamelauncher.core

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

data class BenchmarkResult(
    val cpuScore: Int,
    val gpuScore: Int,
    val memoryScore: Int,
    val overallScore: Int,
    val deviceRating: Int
)

@Singleton
class BenchmarkManager @Inject constructor(
    private val deviceManager: DeviceManager
) {
    suspend fun runBenchmark(): BenchmarkResult = withContext(Dispatchers.Default) {
        val cpuScore = runCpuBenchmark()
        val gpuScore = runGpuBenchmark()
        val memoryScore = runMemoryBenchmark()
        val deviceRating = deviceManager.getDeviceRating()

        BenchmarkResult(
            cpuScore = cpuScore,
            gpuScore = gpuScore,
            memoryScore = memoryScore,
            overallScore = (cpuScore + gpuScore + memoryScore) / 3,
            deviceRating = deviceRating
        )
    }

    private fun runCpuBenchmark(): Int {
        val start = System.nanoTime()
        val data = mutableListOf<Int>()
        for (i in 0 until 500_000) data.add((Math.random() * 100000).toInt())
        Collections.sort(data)
        for (i in 0 until 100_000) {
            Collections.binarySearch(data, (Math.random() * 100000).toInt())
        }
        var primes = 0
        for (i in 2..50000) {
            var isPrime = true
            val sqrt = kotlin.math.sqrt(i.toDouble()).toInt()
            for (j in 2..sqrt) {
                if (i % j == 0) { isPrime = false; break }
            }
            if (isPrime) primes++
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000
        return ((2_000_000 / elapsed.coerceAtLeast(1)).toInt()).coerceIn(0, 2000)
    }

    private fun runGpuBenchmark(): Int {
        val start = System.nanoTime()
        val size = 200
        var totalPixels = 0L
        repeat(50) {
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    val r = (x * 255 / size).coerceIn(0, 255)
                    val g = (y * 255 / size).coerceIn(0, 255)
                    val b = ((x + y) * 255 / (size * 2)).coerceIn(0, 255)
                    bmp.setPixel(x, y, (0xFF shl 24) or (r shl 16) or (g shl 8) or b)
                }
            }
            val pixels = IntArray(size * size)
            bmp.getPixels(pixels, 0, size, 0, 0, size, size)
            totalPixels += pixels.sum()
            bmp.recycle()
        }
        val elapsed = (System.nanoTime() - start) / 1_000_000
        return ((1_000_000 / elapsed.coerceAtLeast(1)).toInt()).coerceIn(0, 2000)
    }

    private fun runMemoryBenchmark(): Int {
        val start = System.nanoTime()
        val arrays = mutableListOf<ByteArray>()
        val chunkSize = 1024 * 512
        var totalBytes = 0L
        try {
            repeat(20) {
                val arr = ByteArray(chunkSize)
                for (i in arr.indices) arr[i] = (i % 256).toByte()
                totalBytes += arr.sum()
                arrays.add(arr)
            }
            arrays.reverse()
            arrays.forEach { it.fill(0) }
        } catch (_: OutOfMemoryError) {}
        arrays.clear()
        System.gc()
        val elapsed = (System.nanoTime() - start) / 1_000_000
        return ((500_000 / elapsed.coerceAtLeast(1)).toInt()).coerceIn(0, 2000)
    }
}
