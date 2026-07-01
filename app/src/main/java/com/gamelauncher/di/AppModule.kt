package com.gamelauncher.di

import android.content.Context
import android.hardware.display.DisplayManager
import androidx.room.Room
import com.gamelauncher.data.local.AppDatabase
import com.gamelauncher.data.local.GameDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * App-wide coroutine scope bound to the process lifetime.
     * SupervisorJob → one child failing won't cancel siblings.
     * Inject via @ApplicationScope wherever you need a long-lived scope
     * instead of creating a raw CoroutineScope() that can't be tracked.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gamelauncher.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideGameDao(database: AppDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    @Singleton
    fun provideDisplayManager(@ApplicationContext context: Context): DisplayManager {
        return context.getSystemService(DisplayManager::class.java)
    }
}
