package com.boardgame.deepdeck

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy.Builder
import androidx.datastore.preferences.core.stringPreferencesKey
import com.boardgame.deepdeck.config.PersistenceSetting
import com.boardgame.deepdeck.onboarding.Constants
import com.boardgame.deepdeck.utils.DataStoreUtils
import com.boardgame.deepdeck.utils.LocaleUtils
import com.boardgame.deepdeck.utils.SoundUtils
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