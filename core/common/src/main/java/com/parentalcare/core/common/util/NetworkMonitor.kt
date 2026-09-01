package com.parentalcare.core.common.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Monitors network connectivity and triggers offline queue processing when online.
 */
class NetworkMonitor(private val context: Context) : LifecycleEventObserver {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Timber.tag("NetworkMonitor").i("Network available - processing offline queue")
            OfflineQueueManager.getInstance(context).startProcessor()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Timber.tag("NetworkMonitor").i("Network lost")
        }
    }

    fun start() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun stop() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_START) {
            start()
        } else if (event == Lifecycle.Event.ON_STOP) {
            stop()
        }
    }

    companion object {
        fun registerWithLifecycle(context: Context, lifecycle: Lifecycle) {
            val monitor = NetworkMonitor(context)
            lifecycle.addObserver(monitor)
        }
    }
}