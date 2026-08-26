package com.boardgame.deepdeck.features.language

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.config.PersistenceSetting
import com.boardgame.deepdeck.onboarding.Constants
import com.boardgame.deepdeck.utils.DataStoreUtils
import com.boardgame.deepdeck.utils.LanguageItem
import com.boardgame.deepdeck.utils.listLanguageSupport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageSelectVM @Inject constructor(
    val dataStoreUtils: DataStoreUtils,
) : ViewModel() {

    companion object {
        val PREF_USER_SETTING = stringPreferencesKey(Constants.APP_INTERNAL_LANGUAGE_PREF)
    }

    fun saveLanguage(language: LanguageItem, onComplete: () -> Unit) {
        viewModelScope.launch {
            val current = dataStoreUtils.getSerializedData(PREF_USER_SETTING, PersistenceSetting::class.java)
                ?: PersistenceSetting()
            if (current.language != language.code) {
                dataStoreUtils.setSerializedData(PREF_USER_SETTING, current.copy(language = language.code))
            }
            onComplete.invoke()
        }
    }

    suspend fun loadLanguage(): LanguageItem {
        val code = dataStoreUtils.getSerializedData(PREF_USER_SETTING, PersistenceSetting::class.java)
            ?.language
            ?: PersistenceSetting().language
        return listLanguageSupport.find { it.code == code } ?: listLanguageSupport[0]
    }
}
