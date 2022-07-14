package dev.nycode.regenbogenice.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

val Instant.isFuture
    get() = this >= Clock.System.now()

val Instant.isPast
    get() = this < Clock.System.now()
