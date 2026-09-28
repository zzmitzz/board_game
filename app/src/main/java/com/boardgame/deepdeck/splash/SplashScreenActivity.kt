package com.boardgame.deepdeck.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.boardgame.deepdeck.MainActivity
import com.boardgame.deepdeck.databinding.ActivitySplashBinding
import com.boardgame.deepdeck.onboarding.OnBoardingActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private val mBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        navigate()
    }

    private fun navigate() {
        lifecycleScope.launch {
            delay(SPLASH_DURATION_MS)
            // Onboarding is shown once; returning users go straight to the app.
            val target = if (hasSeenOnboarding()) MainActivity::class.java else OnBoardingActivity::class.java
            val intent = Intent(this@SplashScreenActivity, target)
            startActivity(intent)
            finish()
        }
    }

    private fun hasSeenOnboarding(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    }

    companion object {
        private const val SPLASH_DURATION_MS = 1200L
        private const val PREFS_NAME = "boardgame_prefs"
        const val KEY_ONBOARDING_DONE = "key_onboarding_done"
    }
}
