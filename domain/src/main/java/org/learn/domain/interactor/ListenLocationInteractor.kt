package org.learn.domain.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.learn.domain.entity.Location
import org.learn.domain.entity.ResponseState
import org.learn.domain.repository.LocationRepository

class ListenLocationInteractor(val locationRepository: LocationRepository)
    : BaseFlowInteractor<Nothing?, Location>(Dispatchers.IO, Dispatchers.Main) {

    override suspend fun buildFlow(params: Nothing?): Flow<ResponseState<Location>> {
        return locationRepository.listenLocationUpdates()
    }
}