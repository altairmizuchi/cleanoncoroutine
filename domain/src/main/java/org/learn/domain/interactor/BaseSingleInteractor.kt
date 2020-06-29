package org.learn.domain.interactor

import kotlinx.coroutines.*
import org.learn.domain.entity.ResponseState
import kotlin.coroutines.EmptyCoroutineContext

abstract class BaseSingleInteractor<in Params, out Result>(
    executeOn: CoroutineDispatcher,
    reportOn: CoroutineDispatcher
) : BaseUseCase(executeOn) {

    private val reportOnContext = EmptyCoroutineContext + reportOn

    fun execute(
        params: Params,
        onResult: (ResponseState<Result>) -> Unit,
        onError: (e: Throwable) -> Unit
    ) {
        executeScope.launch {
            val deferred = async { executeInternal(params) }
            try {
                val result = deferred.await()
                withContext(reportOnContext) {
                    onResult(result)
                }
            } catch (e: Throwable) {
                withContext(reportOnContext) {
                    onError(e)
                }
            }
        }
    }

    abstract suspend fun executeInternal(params: Params): ResponseState<Result>
}