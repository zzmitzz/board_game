package com.alantech.boardgame.utils

import androidx.annotation.DrawableRes
import com.alantech.boardgame.R
import kotlinx.serialization.Serializable


@Serializable
data class LanguageItem(
    val code: String,
    val fullName: String,
    @DrawableRes val flagRes: Int,
) {
    override fun toString(): String = fullName
}

val listLanguageSupport = listOf(
    LanguageItem(code = "en", fullName = "English",    flagRes = R.drawable.ic_usa),
    LanguageItem(code = "hi", fullName = "Hindi",      flagRes = R.drawable.ic_hindi),
    LanguageItem(code = "es", fullName = "Spanish",    flagRes = R.drawable.ic_es),
    LanguageItem(code = "fr", fullName = "French",     flagRes = R.drawable.ic_fr),
    LanguageItem(code = "id", fullName = "Indonesian", flagRes = R.drawable.ic_id),
    LanguageItem(code = "tr", fullName = "Turkish",    flagRes = R.drawable.ic_tr),
    LanguageItem(code = "de", fullName = "German",     flagRes = R.drawable.ic_de),
    LanguageItem(code = "it", fullName = "Italian",    flagRes = R.drawable.ic_it),
    LanguageItem(code = "ja", fullName = "Japanese",   flagRes = R.drawable.ic_ja),
    LanguageItem(code = "ko", fullName = "Korean",     flagRes = R.drawable.ic_korean),
    LanguageItem(code = "pt", fullName = "Portuguese", flagRes = R.drawable.ic_pt),
    LanguageItem(code = "ru", fullName = "Russian",    flagRes = R.drawable.ic_ru),
    LanguageItem(code = "vi", fullName = "Vietnamese", flagRes = R.drawable.ic_vn),
)