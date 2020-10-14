package com.sduduzog.slimlauncher.di

import android.app.Application
import androidx.room.Room
import com.sduduzog.slimlauncher.data.BaseDao
import com.sduduzog.slimlauncher.data.BaseDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class AppModule {
    @Provides
    @Singleton
    internal fun provideBaseDatabase(application: Application): BaseDatabase {
        return Room.databaseBuilder(application,
                BaseDatabase::class.java, "app_database")
                .addMigrations(
                        BaseDatabase.MIGRATION_1_2,
                        BaseDatabase.MIGRATION_2_3,
                        BaseDatabase.MIGRATION_3_4,
                        BaseDatabase.MIGRATION_4_5,
                        BaseDatabase.MIGRATION_5_6,
                        BaseDatabase.MIGRATION_6_7,
                        BaseDatabase.MIGRATION_7_8
                )
                .build()
    }

    @Provides
    @Singleton
    internal fun provideBaseDao(baseDatabase: BaseDatabase): BaseDao {
        return baseDatabase.baseDao()
    }
}