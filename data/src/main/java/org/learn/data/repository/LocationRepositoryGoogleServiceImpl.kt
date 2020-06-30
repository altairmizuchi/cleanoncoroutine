package org.learn.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.learn.domain.entity.Location
import org.learn.domain.entity.ResponseFlow
import org.learn.domain.entity.emitSuccess
import org.learn.domain.repository.LocationRepository

class LocationRepositoryGoogleServiceImpl(val locationDataSource: GoogleServiceLocationDataSource) : LocationRepository {

    @SuppressLint("MissingPermission")
    override fun listenLocationUpdates(): ResponseFlow<Location> = flow {
        val request = LocationRequest().apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val channel = locationDataSource.subscribe(request)

        try {
            coroutineScope {
                while (isActive) {
                    emitSuccess(channel.receive().let { Location(it.latitude, it.longitude) })
                }
            }
        } finally {
            locationDataSource.unsubscribe(request, channel)
        }
    }

    companion object {
        fun getInstance(context: Context) = LocationRepositoryGoogleServiceImpl(
            GoogleServiceLocationDataSource(LocationServices
                .getFusedLocationProviderClient(context))
        )
    }
}