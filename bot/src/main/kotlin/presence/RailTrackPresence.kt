package dev.nycode.regenbogenice.presence

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.core.Kord
import dev.nycode.regenbogenice.train.fetchCurrentTrip
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

private const val REGENBOGEN_ICE_TZN = "304"

class RailTrackPresence : CoroutineScope, KordExKoinComponent {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private val kord by inject<Kord>()
    private val isRunning = Mutex()

    fun start() = launch {
        isRunning.withLock {
            while (isActive) {
                val (_, trip) = fetchCurrentTrip(REGENBOGEN_ICE_TZN)
                    ?: continue
                kord.editPresence {
                    watching("${trip.displayName} to ${trip.destinationStation}")
                }
                delay(5.seconds)
            }
        }
    }
}

private val TrainVehicle.Trip.displayName: String
    get() = "$trainType $trainNumber"
