package dev.nycode.regenbogenice.train

import dev.nycode.regenbogenice.client.RegenbogenICEClient
import dev.nycode.regenbogenice.client.TrainVehicle
import dev.nycode.regenbogenice.client.Trip
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * Fetches the current trip information and prepares it for easier usage.
 */
suspend fun RegenbogenICEClient.fetchCurrentTrip(
    query: String,
    tripLimit: Int = 20,
    includeRoutes: Boolean = true,
    includeMarudorLink: Boolean = true,
): Pair<TrainVehicle, Trip>? {
    val train = fetchTrainVehicle(query, tripLimit, includeRoutes, includeMarudorLink)
    return train to (train.trips?.findCurrentTripOrNull() ?: return null)
}

private fun Collection<Trip>.findCurrentTripOrNull(): Trip? {
    return asSequence().filterNot(Trip::isObsolete).minByOrNull { it.arrival!! }
}

private fun Trip.isObsolete(): Boolean {
    return arrival?.plus(30.minutes)?.let {
        it < Clock.System.now()
    } ?: true
}

/**
 * The arrival at the last stop of the trip.
 */
val Trip.arrival: Instant?
    get() = stops?.last()?.arrival
