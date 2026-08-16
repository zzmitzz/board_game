package com.alantech.boardgame.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alantech.boardgame.MainActivity
import com.alantech.boardgame.databinding.ActivitySplashBinding
import com.alantech.boardgame.onboarding.OnBoardingActivity
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
            val intent = Intent(this@SplashScreenActivity, OnBoardingActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun hasSeenOnboarding(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    }

    companion object {
        private const val SPLASH_DURATION_MS = 2000L
        private const val PREFS_NAME = "boardgame_prefs"
        const val KEY_ONBOARDING_DONE = "key_onboarding_done"
    }
}
