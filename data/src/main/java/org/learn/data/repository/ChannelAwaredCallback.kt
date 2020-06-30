package org.learn.data.repository

import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


private const val TAG = "ChannelAwaredCallback"

class ChannelAwaredCallback : LocationCallback() {
    private val channelsMutex = Mutex()
    private val scope = MainScope()
    private val channels = mutableSetOf<Channel<Location>>()

    override fun onLocationResult(result: LocationResult?) {
        if (result != null) {
            scope.launch {
                result.lastLocation?.let {
                    sendLocation(it)
                }
            }
        }
    }

    private suspend fun sendLocation(location: Location) = channelsMutex.withLock {
        for (c in channels) {
            try {
                c.send(location)
            } catch (error: Throwable) {
                handleSendError(error)
            }
        }
    }

    private fun handleSendError(error: Throwable) {
        Log.e(TAG, "Channel send error", error)
    }

    suspend fun addChannel(channel: Channel<Location>) = channelsMutex.withLock {
        channels.add(channel)
    }

    suspend fun removeChannel(channel: Channel<Location>): Boolean = channelsMutex.withLock {
        channels.remove(channel)
        return channels.isNotEmpty()
    }
}