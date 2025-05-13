@file:OptIn(ExperimentalCoroutinesApi::class)

package com.suhel.mycoolllama.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

fun <T> triggerFlow() = MutableSharedFlow<T?>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

fun unitTriggerFlow() = MutableSharedFlow<Unit>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

fun <T> MutableSharedFlow<T?>.trigger(value: T) = tryEmit(value)

fun MutableSharedFlow<Unit>.trigger() = tryEmit(Unit)

fun <T> Flow<T>.cacheIn(scope: CoroutineScope, initialValue: T, millis: Long = 5_000) =
    stateIn(
        scope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = millis),
        initialValue
    )

fun <T> Flow<T?>.cacheIn(scope: CoroutineScope, millis: Long = 5_000) =
    stateIn(scope, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = millis), null)

fun <T> Flow<T?>.emitNullOnStart() = onStart { emit(null) }

fun <T1, T2, R> Flow<T1>.flatMapCombine(
    flow2: Flow<T2>,
    transform: suspend (T1, T2) -> Flow<R>
): Flow<R> =
    combine(flow2) { t1, t2 -> Pair(t1, t2) }
        .distinctUntilChanged()
        .flatMapLatest { (t1, t2) -> transform(t1, t2) }

fun <T> Flow<T>.takeUntil(other: Flow<*>): Flow<T> = channelFlow {
    launch {
        collect { send(it) }
    }

    launch {
        other.first()
        close()
    }
}
