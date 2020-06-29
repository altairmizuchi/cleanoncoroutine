package org.learn.domain.interactor

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

interface UseCase {
    fun cancel()
}

open class BaseUseCase(executeOn: CoroutineDispatcher) : UseCase {

    protected val executeScope = CoroutineScope(SupervisorJob() + executeOn)

    final override fun cancel() {
        try {
            executeScope.cancel()
        } catch (e: Throwable) {}
    }
}