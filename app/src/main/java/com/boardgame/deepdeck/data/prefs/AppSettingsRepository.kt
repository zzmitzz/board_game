package com.boardgame.deepdeck.data.prefs

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.boardgame.deepdeck.config.PersistenceSetting
import com.boardgame.deepdeck.onboarding.Constants
import com.boardgame.deepdeck.utils.DataStoreUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Unified view of user settings (You tab). */
data class AppSettings(
    /** UI language (app strings). */
    val uiLanguage: String = "en",
    /** Translate card text into [uiLanguage]. */
    val translateCards: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val notificationsEnabled: Boolean = false,
)

/**
 * One place for settings. Historically there were two stores:
 *  - `pref_app_language`  → PersistenceSetting.language = UI language
 *  - `pref_user_setting`  → PersistenceSetting (card language, auto-translate, haptics, sound)
 * Both keys are kept (gameplay reads `pref_user_setting`), but they are written together here
 * so the card language always follows the UI language.
 */
@Singleton
class AppSettingsRepository @Inject constructor(
    private val dataStoreUtils: DataStoreUtils,
) {
    val settings: Flow<AppSettings> = combine(
        dataStoreUtils.getFlow(KEY_APP_LANGUAGE, PersistenceSetting::class.java),
        dataStoreUtils.getFlow(KEY_USER_SETTING, PersistenceSetting::class.java),
        dataStoreUtils.getFlow(KEY_NOTIFICATIONS),
    ) { app, user, notifications ->
        val userSetting = user ?: PersistenceSetting()
        AppSettings(
            uiLanguage = app?.language ?: PersistenceSetting().language,
            translateCards = userSetting.isAutoTranslate,
            hapticsEnabled = userSetting.isHapticOn,
            soundEnabled = userSetting.isSoundOn,
            notificationsEnabled = notifications ?: false,
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun cardSetting(): PersistenceSetting =
        dataStoreUtils.getSerializedData(KEY_USER_SETTING, PersistenceSetting::class.java)
            ?: PersistenceSetting()

    /** Language used for content requests (`?lang=`). */
    suspend fun contentLanguage(): String = current().uiLanguage

    /** Saves UI language + "translate cards" in both legacy stores. */
    suspend fun setLanguage(code: String, translateCards: Boolean) {
        val app = dataStoreUtils.getSerializedData(KEY_APP_LANGUAGE, PersistenceSetting::class.java)
            ?: PersistenceSetting()
        dataStoreUtils.setSerializedData(KEY_APP_LANGUAGE, app.copy(language = code))
        val user = cardSetting()
        dataStoreUtils.setSerializedData(
            KEY_USER_SETTING,
            user.copy(language = code, isAutoTranslate = translateCards && code != "en")
        )
    }

    suspend fun setHaptics(enabled: Boolean) {
        dataStoreUtils.setSerializedData(KEY_USER_SETTING, cardSetting().copy(isHapticOn = enabled))
    }

    suspend fun setSound(enabled: Boolean) {
        dataStoreUtils.setSerializedData(KEY_USER_SETTING, cardSetting().copy(isSoundOn = enabled))
    }

    suspend fun setNotifications(enabled: Boolean) {
        dataStoreUtils.setPrimitiveData(KEY_NOTIFICATIONS, enabled)
    }

    /**
     * Language for card requests (`/packs/cards?lang=`): the UI language when
     * "Translate cards" is on, otherwise English (the content's source language).
     */
    suspend fun cardLanguage(): String {
        val setting = cardSetting()
        return if (setting.isAutoTranslate && setting.language.isNotBlank()) setting.language else "en"
    }

    /** Vibe picked on the last onboarding page ("Who do you usually play with?"), as an icon key. */
    val preferredVibe: Flow<String?> = dataStoreUtils.getFlow(KEY_PREFERRED_VIBE)

    suspend fun setPreferredVibe(iconKey: String?) {
        if (iconKey == null) return
        dataStoreUtils.setPrimitiveData(KEY_PREFERRED_VIBE, iconKey)
    }

    companion object {
        val KEY_APP_LANGUAGE = stringPreferencesKey(Constants.APP_INTERNAL_LANGUAGE_PREF)
        val KEY_USER_SETTING = stringPreferencesKey("pref_user_setting")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("pref_notifications_enabled")
        val KEY_PREFERRED_VIBE = stringPreferencesKey("pref_preferred_vibe")
    }
}
