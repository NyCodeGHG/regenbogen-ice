package dev.nycode.regenbogenice.train

import dev.nycode.regenbogenice.command.TrainTripResult
import dev.nycode.regenbogenice.command.fetchTrain
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * Fetches the current trip information and prepares it for easier usage.
 */
suspend fun fetchCurrentTrip(
    query: String,
    tripLimit: Int = 20,
    includeRoutes: Boolean = true,
    includeMarudorLink: Boolean = true,
): TrainTripResult? {
    val train = fetchTrain(query, tripLimit, includeRoutes, includeMarudorLink) ?: return null
    return TrainTripResult(train, (train.trips?.findCurrentTripOrNull() ?: return null))
}

private fun Collection<TrainVehicle.Trip>.findCurrentTripOrNull(): TrainVehicle.Trip? {
    return asSequence().filterNot(TrainVehicle.Trip::isObsolete).minByOrNull { it.arrival!! }
}

private fun TrainVehicle.Trip.isObsolete(): Boolean {
    return arrival?.plus(30.minutes)?.let {
        it < Clock.System.now()
    } ?: true
}

/**
 * The arrival at the last stop of the trip.
 */
val TrainVehicle.Trip.arrival: Instant?
    get() = stops.last().arrival
