package com.gamelauncher.core.shizuku

/**
 * ShizukuWriteException — Strongly typed exception thrown when Shizuku settings or system writes fail.
 */
class ShizukuWriteException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
