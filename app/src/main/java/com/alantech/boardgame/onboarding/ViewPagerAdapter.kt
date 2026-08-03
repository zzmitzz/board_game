package com.alantech.boardgame.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.alantech.boardgame.onboarding.fragments.OnboardingFragment

class ViewPagerAdapter(
    activity: FragmentActivity,
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = listOnboardingFill.size

    override fun createFragment(position: Int): Fragment =
        OnboardingFragment.newInstance(position)
}