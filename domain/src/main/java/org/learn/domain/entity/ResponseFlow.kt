package org.learn.domain.entity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

typealias ResponseFlow<T> = Flow<ResponseState<T>>

suspend fun <T> FlowCollector<ResponseState<T>>.emitSuccess(element: T) = emit(Success(element))
suspend fun <T> FlowCollector<ResponseState<T>>.emitFailure(error: String) = emit(Failure(error))