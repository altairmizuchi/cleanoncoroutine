package org.learn.domain.interactor

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.learn.domain.entity.ResponseState
import kotlin.coroutines.EmptyCoroutineContext

abstract class BaseFlowInteractor<in Params, out Result>(
    executeOn: CoroutineDispatcher,
    reportOn: CoroutineDispatcher
) : BaseUseCase(executeOn) {

    private val reportOnContext = EmptyCoroutineContext + reportOn

    fun execute(
        params: Params,
        onUpdate: (ResponseState<Result>) -> Unit,
        onError: (e: Throwable) -> Unit
    ) {
        executeScope.launch {
            val deferredFlow = async { buildFlow(params) }
            try {
                deferredFlow.await()
                    .catch { error ->
                        withContext(reportOnContext) {
                            onError(error)
                        }
                    }
                    .collect { response ->
                        withContext(reportOnContext) {
                            onUpdate(response)
                        }
                    }
            } catch (e: Throwable) {
                withContext(reportOnContext) {
                    onError(e)
                }
            }
        }
    }

    abstract suspend fun buildFlow(params: Params): Flow<ResponseState<Result>>
}