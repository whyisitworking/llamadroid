package com.suhel.mycoolllama.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

fun <T> triggerFlow() = MutableSharedFlow<T?>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

fun unitTriggerFlow() = MutableSharedFlow<Unit>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

fun <T> MutableSharedFlow<T?>.trigger(value: T?) = tryEmit(value)

fun MutableSharedFlow<Unit>.trigger() = tryEmit(Unit)

fun <T> Flow<T>.cacheIn(scope: CoroutineScope, initialValue: T, millis: Long = 5_000) =
    stateIn(scope, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = millis), initialValue)

fun <T> Flow<T?>.cacheIn(scope: CoroutineScope, millis: Long = 5_000) =
    stateIn(scope, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = millis), null)

fun <T> Flow<T?>.emitNullOnStart() = onStart { emit(null) }
