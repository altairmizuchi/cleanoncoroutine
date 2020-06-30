package org.learn.domain.repository

import org.learn.domain.entity.Location
import org.learn.domain.entity.ResponseFlow

interface LocationRepository {

    fun listenLocationUpdates(): ResponseFlow<Location>
}