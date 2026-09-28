package com.boardgame.deepdeck.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.boardgame.deepdeck.onboarding.fragments.OnboardingFinalFragment
import com.boardgame.deepdeck.onboarding.fragments.OnboardingFragment

class ViewPagerAdapter(
    activity: FragmentActivity,
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = listOnboardingFill.size

    /** The last page is the Compose "Who do you usually play with?" + reminder opt-in. */
    override fun createFragment(position: Int): Fragment =
        if (position == listOnboardingFill.lastIndex) OnboardingFinalFragment()
        else OnboardingFragment.newInstance(position)
}