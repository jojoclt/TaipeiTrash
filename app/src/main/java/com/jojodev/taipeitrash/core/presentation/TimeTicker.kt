package com.jojodev.taipeitrash.core.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime

/**
 * Emits the current LocalDateTime aligned to the device minute boundary.
 * First emission is immediate (current time), subsequent emissions happen on each minute tick.
 */
val minuteTicker: Flow<LocalDateTime> = flow {
    while (true) {
        val now = LocalDateTime.now()
        emit(now)

        val delayMillis = 60_000 - (now.second * 1_000L + now.nano / 1_000_000L)
        // if delayMillis is <= 0, just delay 1 minute
        delay(if (delayMillis > 0) delayMillis else 60_000L)
    }
}

