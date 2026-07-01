package com.gamelauncher.di

import javax.inject.Qualifier

/**
 * Hilt qualifier for the app-wide coroutine scope.
 *
 * Tied to the process lifetime via [SingletonComponent]. Inject this
 * instead of creating a raw CoroutineScope(…) to avoid leaking unscoped
 * jobs that can't be cancelled.
 *
 * Usage:
 *   @Inject @ApplicationScope lateinit var appScope: CoroutineScope
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
