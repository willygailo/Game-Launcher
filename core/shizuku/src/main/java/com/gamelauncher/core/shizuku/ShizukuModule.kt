package com.gamelauncher.core.shizuku

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ShizukuModule — Hilt module binding IShizukuManager and IShellExecutor interfaces to concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ShizukuModule {

    @Binds
    @Singleton
    abstract fun bindShizukuManager(impl: ShizukuManager): IShizukuManager

    @Binds
    @Singleton
    abstract fun bindShellExecutor(impl: ShellExecutor): IShellExecutor
}
