package xyz.techmush.birdhouse_peeper.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import xyz.techmush.birdhouse_peeper.R
import xyz.techmush.birdhouse_peeper.util.extractPermissions
import xyz.techmush.birdhouse_peeper.util.missingPermissions


@AndroidEntryPoint
class BirdhouseActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkIntroFirst()

        setContentView(R.layout.birdhouse_activity)

        appBarConfiguration = AppBarConfiguration.Builder(R.id.BirdhouseListFragment).build()
        val navController = findNavController(R.id.fragmentContainerView)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }


    private fun checkIntroFirst() {
        val missingPermissions = missingPermissions(this, extractPermissions(this))
        val showIntro = getSharedPreferences(packageName, Context.MODE_PRIVATE)
            .getBoolean(IntroActivity.ShowIntro, true)

        if (missingPermissions.isNotEmpty() || showIntro) {
            startActivity(Intent(this, IntroActivity::class.java))
        }
    }
}