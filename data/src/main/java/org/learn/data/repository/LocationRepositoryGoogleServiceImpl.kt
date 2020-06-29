package org.learn.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.learn.domain.entity.Location
import org.learn.domain.entity.ResponseState
import org.learn.domain.entity.Success
import org.learn.domain.repository.LocationRepository
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.EmptyCoroutineContext

class LocationRepositoryGoogleServiceImpl(val provider: FusedLocationProviderClient) : LocationRepository {

    val refLocationFlow = AtomicReference<Flow<ResponseState<Location>>>(null)

    override fun listenLocationUpdates(): Flow<ResponseState<Location>> {
        refLocationFlow.compareAndSet(null, buildLocationFlow())
        return refLocationFlow.get()
    }

    @SuppressLint("MissingPermission")
    private fun buildLocationFlow(): Flow<ResponseState<Location>> {
        val channel = Channel<ResponseState<Location>>()
        //val callback = CoroutineScopeAwaredCallback()
        val callback = ChannelAwaredCallback(channel)

        return flow<ResponseState<Location>> {
            try {
                val request = LocationRequest().apply {
                    interval = 1000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
        /*        coroutineScope {
                    callback.flowCollector = this@flow
                    callback.scope = this
                }
        */        provider.requestLocationUpdates(
                    request,
                    callback,
                    Looper.getMainLooper()
                )
                coroutineScope {
                    while (isActive) {
                        emit(channel.receive())
                    }
                }
            } finally {
                Log.d("ANTON", "STOPPING LOCATION")
                channel.close()
                provider.removeLocationUpdates(callback)
            }
        }
    }

    inner class CoroutineScopeAwaredCallback() : LocationCallback() {
        var scope: CoroutineScope? = null
        var flowCollector: FlowCollector<ResponseState<Location>>? = null
        override fun onLocationResult(result: LocationResult?) {
            result?.lastLocation
                ?.let { Success(Location(it.latitude, it.longitude)) }
                ?.let {
                    Log.d("ANTON", "LOCATION UPDATE $it")
                    try {
                        scope?.launch { flowCollector?.emit(it) }
                    } catch (t: Throwable) {
                    }
                }
        }
    }

    inner class ChannelAwaredCallback(val channel: Channel<ResponseState<Location>>) : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            result?.lastLocation
                ?.let { Success(Location(it.latitude, it.longitude)) }
                ?.let {
                    Log.d("ANTON", "LOCATION UPDATE $it")
                    try {
                        channel.offer(it)
                    } catch (t: Throwable) { }
                }
        }
    }

    companion object {
        fun getInstance(context: Context) = LocationRepositoryGoogleServiceImpl(LocationServices.getFusedLocationProviderClient(context))
    }
}

fun <T> sharedFlow(
    sourceFlowDispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend FlowCollector<T>.() -> Unit
): SharedFlow<T> {
    return SharedFlowImpl(flow(block), sourceFlowDispatcher)
}

interface SharedFlow<T> {
    fun subscribe(): Flow<T>
}

class SharedFlowImpl<T>(source: Flow<T>, dispatcher: CoroutineDispatcher) : SharedFlow<T> {

    private val children = mutableSetOf<Channel<T>>()


    val mutex = Mutex()
    private val sourceFlowScope = CoroutineScope(SupervisorJob() + dispatcher)
    private val source = source.flowOn(EmptyCoroutineContext + dispatcher)

    override fun subscribe(): Flow<T> = flow {
        val channel = Channel<T>()
        mutex.withLock {
            children.add(channel)
            if (children.size == 1) {
                startSourceFlow()
            }
        }
        try {
            coroutineScope {
                while (isActive) {
                    emit(channel.receive())
                }
            }
        } finally {
            mutex.withLock {
                children.remove(channel)
                channel.close()
            }
        }
    }

    private fun startSourceFlow() {
        sourceFlowScope.launch {
            source.collect { value ->
                mutex.withLock {
                    while (children.isNotEmpty()) {
                        for (child in children) {
                            child.send(value)
                        }
                    }
                }
            }
        }
    }
}