package com.gamelauncher.feature.monitor.di

import com.gamelauncher.feature.monitor.data.IMonitorRepository
import com.gamelauncher.feature.monitor.data.MonitorRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * MonitorModule — Hilt DI module binding IMonitorRepository interface to MonitorRepositoryImpl.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MonitorModule {

    @Binds
    @Singleton
    abstract fun bindMonitorRepository(impl: MonitorRepositoryImpl): IMonitorRepository
}
