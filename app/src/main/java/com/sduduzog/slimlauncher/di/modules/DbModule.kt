package com.sduduzog.slimlauncher.di.modules

import android.app.Application
import androidx.room.Room
import com.sduduzog.slimlauncher.data.BaseDao
import com.sduduzog.slimlauncher.data.BaseDatabase
import com.sduduzog.slimlauncher.data.BaseDatabase.Companion.MIGRATION_1_2
import com.sduduzog.slimlauncher.data.BaseDatabase.Companion.MIGRATION_2_3
import com.sduduzog.slimlauncher.data.BaseDatabase.Companion.MIGRATION_3_4
import com.sduduzog.slimlauncher.data.BaseDatabase.Companion.MIGRATION_4_5
import com.sduduzog.slimlauncher.data.BaseDatabase.Companion.MIGRATION_5_6
import com.sduduzog.slimlauncher.data.BaseDatabase.Companion.MIGRATION_6_7
import com.sduduzog.slimlauncher.data.BaseDatabase.Companion.MIGRATION_7_8
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class DbModule {
    @Provides
    @Singleton
    internal fun provideBaseDatabase(application: Application) : BaseDatabase{
        return Room.databaseBuilder(application,
                        BaseDatabase::class.java, "app_database")
                .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8
                )
                .build()
    }

    @Provides
    @Singleton
    internal fun provideBaseDao(baseDatabase: BaseDatabase) : BaseDao {
        return baseDatabase.baseDao()
    }
}