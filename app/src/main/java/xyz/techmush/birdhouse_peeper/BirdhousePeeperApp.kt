package xyz.techmush.birdhouse_peeper

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


@HiltAndroidApp
class BirdhousePeeperApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree()) // Timber logging in Debug mode
        }
    }

} // DatascienceApp
