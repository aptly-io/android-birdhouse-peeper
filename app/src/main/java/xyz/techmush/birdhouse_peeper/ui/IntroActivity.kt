package xyz.techmush.birdhouse_peeper.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import dagger.hilt.android.AndroidEntryPoint
import xyz.techmush.birdhouse_peeper.R
import xyz.techmush.birdhouse_peeper.util.extractPermissions
import xyz.techmush.birdhouse_peeper.util.missingPermissions


@AndroidEntryPoint
class IntroActivity : AppIntro() {

    companion object {
        const val ShowIntro = "SHOW_INTRO"
    }

    private lateinit var sharedPrefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPrefs = this.getSharedPreferences(packageName, Context.MODE_PRIVATE)
        val missingPermissions = missingPermissions(this, extractPermissions(this))
        val showIntro = sharedPrefs.getBoolean(ShowIntro, true)

        if (missingPermissions.isNotEmpty() || showIntro) {

            var slideCount = 0

            if (showIntro) {
                addSlide(AppIntroFragment.newInstance(
                        "Birdhouse Peeper",
                        "Follow your winged tenants raising their chicks!",
                        imageDrawable = R.drawable.birdhouse,
                        backgroundDrawable = R.drawable.back_slide5))
                slideCount++
            }

            if (missingPermissions.isNotEmpty()) {
                addSlide(AppIntroFragment.newInstance(
                    "Connecting with Bluetooth and WiFi",
                    "This requires permissions. Please grant the permissions from the system dialog.",
                    backgroundDrawable = R.drawable.back_slide4))
                slideCount++

                askForPermissions(missingPermissions, slideCount, true)
            }

            isSkipButtonEnabled = missingPermissions.isEmpty()
            setImmersiveMode()
        } else {
            finish()
        }
    }


    public override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }


    // for some reason the DONE button is not shown on the LG G5:
    //   setting is not stored and need odd way to return to calling activity
    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        sharedPrefs.edit().putBoolean(ShowIntro, false).apply()
        finish()
    }
}