package com.gamelauncher.feature.tweaks.di

import com.gamelauncher.feature.tweaks.data.ITweaksRepository
import com.gamelauncher.feature.tweaks.data.TweaksRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * TweaksModule — Hilt DI module binding ITweaksRepository interface to TweaksRepositoryImpl.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TweaksModule {

    @Binds
    @Singleton
    abstract fun bindTweaksRepository(impl: TweaksRepositoryImpl): ITweaksRepository
}
