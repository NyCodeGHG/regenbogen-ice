package dev.nycode.regenbogenice.client

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TrainVehicle(
    @SerialName("building_series")
    val buildingSeries: Int,
    val name: String,
    val number: Int,
    @SerialName("train_type")
    val trainType: String,
    val trips: List<Trip>? = null,
)

@Serializable
public data class Trip(
    @SerialName("destination_station")
    val destinationStation: String,
    @SerialName("group_index")
    val groupIndex: Int,
    @SerialName("initial_departure")
    val initialDeparture: Instant,
    @SerialName("origin_station")
    val originStation: String,
    @SerialName("train_number")
    val trainNumber: Int,
    @SerialName("train_type")
    val trainType: String,
    @SerialName("trip_timestamp")
    val tripTimestamp: Instant,
    @SerialName("vehicle_timestamp")
    val vehicleTimestamp: Instant,
    val marudor: String?,
    val stops: List<Stop>? = null,
)

@Serializable
public data class Stop(
    val arrival: Instant?,
    val cancelled: Int,
    val departure: Instant?,
    @SerialName("scheduled_arrival")
    val scheduledArrival: Instant?,
    @SerialName("scheduled_departure")
    val scheduledDeparture: Instant?,
    val station: String,
)
