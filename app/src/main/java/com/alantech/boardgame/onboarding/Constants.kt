package com.alantech.boardgame.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.alantech.boardgame.R


data class OnboardingItem(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val image: Int
)

val listOnboardingFill: List<OnboardingItem> = listOf(
    OnboardingItem(
        title = R.string.onboarding_title_1,
        description = R.string.onboarding_description_1,
        image = R.drawable.onboard_screen1_friends_1785742095301,
    ),
    OnboardingItem(
        title = R.string.onboarding_title_2,
        description = R.string.onboarding_description_2,
        image = R.drawable.onboard_screen2_packs_1785742118883,
    ),
    OnboardingItem(
        title = R.string.onboarding_title_3,
        description = R.string.onboarding_description_3,
        image = R.drawable.onboard_screen3_getstarted_1785742139974,
    )
)


object Constants {
    val APP_INTERNAL_LANGUAGE_PREF = "pref_app_language"
}