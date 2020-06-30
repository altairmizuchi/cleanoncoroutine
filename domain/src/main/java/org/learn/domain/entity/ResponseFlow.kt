package org.learn.domain.entity

import kotlinx.coroutines.flow.Flow

typealias ResponseFlow<T> = Flow<ResponseState<T>>