package org.learn.data.repository

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GoogleServiceLocationDataSource(
    private val locationProviderClient: FusedLocationProviderClient
) {
    private val mapMutex = Mutex()
    private val callbackMap = mutableMapOf<LocationRequest, ChannelAwaredCallback>()

    suspend fun subscribe(request: LocationRequest): Channel<Location> {
        val result = Channel<Location>()
        mapMutex.withLock {
            callbackMap[request]?.addChannel(result) ?: createNewCallback(request, result)
        }
        return result
    }

    @SuppressLint("MissingPermission")
    private suspend fun createNewCallback(request: LocationRequest, channel: Channel<Location>) {
        ChannelAwaredCallback().apply {
            callbackMap[request] = this
            addChannel(channel)
            locationProviderClient.requestLocationUpdates(request, this, Looper.getMainLooper())
        }
    }

    suspend fun unsubscribe(request: LocationRequest, channel: Channel<Location>) {
        mapMutex.withLock {
            val callback = callbackMap[request]
            if (callback != null && !callback.removeChannel(channel)) {
                callbackMap.remove(request)
                locationProviderClient.removeLocationUpdates(callback)
            }
        }
        channel.close()
    }
}