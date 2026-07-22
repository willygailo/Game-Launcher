package com.gamelauncher.core.database.di

import android.content.Context
import androidx.room.Room
import com.gamelauncher.core.database.BuildConfig
import com.gamelauncher.core.database.GameProfileDao
import com.gamelauncher.core.database.GamingDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DatabaseModule — Hilt DI module providing GamingDatabase and GameProfileDao singletons.
 * Destructive migration is strictly guarded by BuildConfig.DEBUG using Kotlin .apply scope function
 * to guarantee robust method-chaining execution across all Room builder semantics.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGamingDatabase(
        @ApplicationContext context: Context
    ): GamingDatabase {
        return Room.databaseBuilder(
            context,
            GamingDatabase::class.java,
            GamingDatabase.DATABASE_NAME
        ).apply {
            if (BuildConfig.DEBUG) {
                fallbackToDestructiveMigration(dropAllTables = true)
            }
        }.build()
    }

    @Provides
    @Singleton
    fun provideGameProfileDao(
        database: GamingDatabase
    ): GameProfileDao {
        return database.gameProfileDao()
    }
}
