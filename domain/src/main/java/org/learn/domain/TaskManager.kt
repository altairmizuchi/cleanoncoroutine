package org.learn.domain

import kotlinx.coroutines.CoroutineDispatcher

enum class Strategy {
    NO_CANCEL, CANCEL_PREVIOUS, CANCEL_NEW
}

class TaskManager(val executeOn: CoroutineDispatcher, val reportOn: CoroutineDispatcher) {

    fun execute(
        taskKey: String,
        strategy: Strategy
    ) {

    }

    fun cancel(taskKey: String) {

    }
}