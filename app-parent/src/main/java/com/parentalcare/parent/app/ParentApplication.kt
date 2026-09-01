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
        
        try {
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setProjectId(getString(com.parentalcare.parent.R.string.firebase_project_id))
                .setApplicationId(getString(com.parentalcare.parent.R.string.firebase_application_id))
                .setApiKey(getString(com.parentalcare.parent.R.string.firebase_api_key))
                .setGcmSenderId(getString(com.parentalcare.parent.R.string.firebase_sender_id))
                .setStorageBucket(getString(com.parentalcare.parent.R.string.firebase_storage_bucket))
                .build()
            FirebaseApp.initializeApp(this, options)
        } catch (e: Exception) {
            Timber.e(e, "Firebase init failed")
        }

        if (com.parentalcare.parent.BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        NotificationChannelInitializer.initialize(this)
    }
}
