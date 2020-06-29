package org.learn.domain.repository

import kotlinx.coroutines.flow.Flow
import org.learn.domain.entity.Location
import org.learn.domain.entity.ResponseState

interface LocationRepository {

    fun listenLocationUpdates(): Flow<ResponseState<Location>>
}