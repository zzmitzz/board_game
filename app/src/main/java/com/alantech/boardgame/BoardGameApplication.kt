package com.alantech.boardgame

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy.Builder
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alantech.boardgame.config.PersistenceSetting
import com.alantech.boardgame.onboarding.Constants
import com.alantech.boardgame.utils.DataStoreUtils
import com.alantech.boardgame.utils.LocaleUtils
import com.alantech.boardgame.utils.SoundUtils
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class BoardGameApplication : Application() {

    @Inject
    lateinit var dataStoreUtils: DataStoreUtils

    private fun isDebuggable(): Boolean {
        return 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
    }


    private fun setStrictModePolicy() {
        if (isDebuggable()) {
            StrictMode.setThreadPolicy(
                Builder().detectAll().penaltyLog().build(),
            )
        }
    }

    fun getLanguageCode(): String {
        val key = stringPreferencesKey(Constants.APP_INTERNAL_LANGUAGE_PREF)
        return runBlocking {
            dataStoreUtils.getSerializedData(key, PersistenceSetting::class.java)?.language
                ?: PersistenceSetting().language
        }
    }

    fun applyStoredLocale() {
        LocaleUtils.setLocale(this, getLanguageCode())
    }

    override fun onCreate() {
        super.onCreate()
        setStrictModePolicy()
        SoundUtils.init(this)
        applyStoredLocale()
    }

    override fun onTerminate() {
        super.onTerminate()
        SoundUtils.release()
    }
}