package com.gamelauncher.feature.network.di

import com.gamelauncher.feature.network.data.INetworkRepository
import com.gamelauncher.feature.network.data.NetworkRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * NetworkModule — Hilt DI module binding INetworkRepository interface to NetworkRepositoryImpl.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(impl: NetworkRepositoryImpl): INetworkRepository
}
