package com.neon.peggame.di

import android.content.Context
import androidx.room.Room
import com.neon.peggame.data.AppDatabase
import com.neon.peggame.data.ScoreDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "peg_neon_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideScoreDao(database: AppDatabase): ScoreDao {
        return database.scoreDao()
    }
}
