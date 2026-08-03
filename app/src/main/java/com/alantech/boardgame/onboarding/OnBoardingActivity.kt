package com.alantech.boardgame.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.alantech.boardgame.MainActivity
import com.alantech.boardgame.R
import com.alantech.boardgame.databinding.ActivityOnboadingBinding
import com.alantech.boardgame.onboarding.fragments.OnboardingFragment

class OnBoardingActivity : AppCompatActivity(), OnboardingFragment.OnboardingCompleteListener {

    private val mBinding by lazy {
        ActivityOnboadingBinding.inflate(layoutInflater)
    }

    private lateinit var dots: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        mBinding.viewPager.adapter = adapter
        setupDots(0)
        mBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
            }
        })
    }

    private fun setupDots(selectedIndex: Int) {
        mBinding.dotsContainer.removeAllViews()
        dots = List(listOnboardingFill.size) { index ->
            ImageView(this).apply {
                val size = resources.getDimensionPixelSize(R.dimen.onboarding_dot_size)
                val margin = resources.getDimensionPixelSize(R.dimen.onboarding_dot_margin)
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, 0, margin, 0)
                }
                setImageResource(
                    if (index == selectedIndex) R.drawable.ic_selected_star
                    else R.drawable.ic_unselected_star
                )
                mBinding.dotsContainer.addView(this)
            }
        }
    }

    private fun updateDots(selectedIndex: Int) {
        dots.forEachIndexed { index, imageView ->
            imageView.setImageResource(
                if (index == selectedIndex) R.drawable.ic_selected_star
                else R.drawable.ic_unselected_star
            )
        }
    }

    override fun onOnboardingComplete() {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(com.alantech.boardgame.splash.SplashScreenActivity.KEY_ONBOARDING_DONE, true)
            .apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val PREFS_NAME = "boardgame_prefs"
    }
}