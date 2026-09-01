package com.parentalcare.parent.app

import android.app.Application
import com.parentalcare.core.notifications.NotificationChannelInitializer
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.google.firebase.FirebaseApp

@HiltAndroidApp
class ParentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        if (com.parentalcare.parent.BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        NotificationChannelInitializer.initialize(this)
    }
}
