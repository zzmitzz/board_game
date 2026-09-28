package com.boardgame.deepdeck.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton




sealed class DataStoreType {
    @Serializable
    object UserSetting : DataStoreType()
}


@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /** Stable file name. */
    private const val FILE_NAME = "user_setting"

    /**
     * The file used to be named `DataStoreType.UserSetting.toString()`, which contains an
     * identity hash (`...$UserSetting@1a2b3c`) that changes on every process start — so every
     * launch opened an empty store and settings/streak/device id/roster were lost. We now use a
     * fixed name and, once, adopt the most recently written legacy file.
     */
    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = {
                val target = context.preferencesDataStoreFile(FILE_NAME)
                if (!target.exists()) migrateLegacyFile(target)
                target
            }
        )
    }

    private fun migrateLegacyFile(target: java.io.File) {
        runCatching {
            val dir = target.parentFile ?: return
            val legacy = dir.listFiles { f ->
                f.name.startsWith(LEGACY_PREFIX) && f.name.endsWith(".preferences_pb")
            }?.maxByOrNull { it.lastModified() } ?: return
            legacy.copyTo(target, overwrite = false)
            dir.listFiles { f -> f.name.startsWith(LEGACY_PREFIX) }?.forEach { it.delete() }
        }
    }

    private val LEGACY_PREFIX = DataStoreType.UserSetting::class.java.name + "@"
}