package com.parentalcare.child.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.parentalcare.child.mediaprojection.ScreenCaptureManager
import com.parentalcare.core.notifications.NotificationChannelInitializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import com.google.firebase.FirebaseApp
import javax.inject.Inject

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChildEntryPoint {
    fun screenCaptureManager(): ScreenCaptureManager
}

@HiltAndroidApp
class ChildApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    val hiltEntryPoint: ChildEntryPoint by lazy {
        dagger.hilt.android.EntryPointAccessors.fromApplication(this, ChildEntryPoint::class.java)
    }

    override fun onCreate() {
        super.onCreate()
        
        try {
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setProjectId(getString(com.parentalcare.child.R.string.firebase_project_id))
                .setApplicationId(getString(com.parentalcare.child.R.string.firebase_application_id))
                .setApiKey(getString(com.parentalcare.child.R.string.firebase_api_key))
                .setGcmSenderId(getString(com.parentalcare.child.R.string.firebase_sender_id))
                .setStorageBucket(getString(com.parentalcare.child.R.string.firebase_storage_bucket))
                .build()
            FirebaseApp.initializeApp(this, options)
        } catch (e: Exception) {
            Timber.e(e, "Firebase init failed")
        }

        if (com.parentalcare.child.BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        NotificationChannelInitializer.initialize(this)
    }

    override val workManagerConfiguration: Configuration by lazy {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
