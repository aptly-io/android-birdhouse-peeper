package xyz.techmush.birdhouse_peeper.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import dagger.hilt.android.AndroidEntryPoint
import xyz.techmush.birdhouse_peeper.R


@AndroidEntryPoint
class IntroActivity : AppIntro() {

    companion object {
        private val requiredPermissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private const val ShowIntro = "SHOW_INTRO"
    }

    private lateinit var sharedPrefs: SharedPreferences


    private fun getMissingPermissions(requiredPermissions: Array<String>) =
        requiredPermissions.filter { PackageManager.PERMISSION_GRANTED != checkSelfPermission(it) }
            .toTypedArray()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPrefs = this.getSharedPreferences(packageName, Context.MODE_PRIVATE)
        val missingPermissions = getMissingPermissions(requiredPermissions)
        val showIntro = sharedPrefs.getBoolean(ShowIntro, true)

        if (missingPermissions.isNotEmpty() || showIntro) {

            setImmersiveMode()

            var slideCount = 0

            if (showIntro) {
                addSlide(AppIntroFragment.newInstance(
                        "Birdhouse Peeper",
                        "Follow your winged tenants raising their chicks!",
                        imageDrawable = R.drawable.birdhouse,
                        backgroundDrawable = R.drawable.back_slide5
                ))
                slideCount++
            }

            if (missingPermissions.isNotEmpty()) {
                addSlide(AppIntroFragment.newInstance(
                    "Connecting with Bluetooth and WiFi",
                    "This requires permissions. Please grant the permissions from the system dialog.",
                    backgroundDrawable = R.drawable.back_slide4
                ))
                slideCount++

                askForPermissions(missingPermissions, slideCount, true)
            }
            isSkipButtonEnabled = missingPermissions.isEmpty()

            addSlide(AppIntroFragment.newInstance(
                "dummy",
                "Follow your winged tenants raising their chicks!"))


        } else {
            startActivity(Intent(this, BirdhouseActivity::class.java))
        }
    }


    public override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        startActivity(Intent(this, BirdhouseActivity::class.java))

    }

    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        sharedPrefs.edit().putBoolean(ShowIntro, false).apply()
        startActivity(Intent(this, BirdhouseActivity::class.java))
    }
}