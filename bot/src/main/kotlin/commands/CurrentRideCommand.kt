package dev.nycode.regenbogenice.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import dev.nycode.regenbogenice.RegenbogenICEExtension
import dev.nycode.regenbogenice.client.RegenbogenICEClient
import dev.nycode.regenbogenice.client.Stop
import dev.nycode.regenbogenice.client.TrainVehicle
import dev.nycode.regenbogenice.client.Trip
import dev.nycode.regenbogenice.train.TrainOverride
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.JourneyInformation
import dev.schlaubi.mikbot.plugin.api.util.discordError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.minutes

class CurrentLocationCommandArguments : Arguments(), KordExKoinComponent {

    private val client by inject<RegenbogenICEClient>()

    val name by defaultingString {
        name = "train"
        description = "The train whose current ride is to be displayed."
        defaultValue = "304"
        autoComplete {
            val trains = client.autoComplete(focusedOption.value)
            suggestString {
                for (train in trains) {
                    choice(train, train)
                }
            }
        }
    }
}

suspend fun RegenbogenICEExtension.currentRideCommand() =
    publicSlashCommand(::CurrentLocationCommandArguments) {
        name = "commands.current_ride.name"
        description = "commands.current_ride.description"

        val client by inject<RegenbogenICEClient>()
        val marudor by inject<Marudor>()

        fun CoroutineScope.fetchMarudorUrlAsync(
            stops: List<Stop>,
            detailsDeferred: Deferred<JourneyInformation?>,
            index: Int,
        ) = async {
            val defaultText = stops[index].station
            val details = detailsDeferred.await() ?: return@async defaultText
            val id = details.stops[index].station.id

            val url = marudor.hafas.detailsRedirect(details.journeyId) {
                parameters.append("stopEva", id)
            }
            "[$defaultText]($url)"
        }

        action {
            val scope = CoroutineScope(Dispatchers.IO)
            val train = client.fetchTrainVehicle(
                arguments.name, 20,
                includeRoutes = true,
                includeMarudorLink = true
            )
            val currentTrip =
                train.trips?.findCurrentTripOrNull() ?: discordError("No trips could be found.")
            val stops = currentTrip.stops ?: discordError("The fetched trip has no routes.")
            val details = scope.async {
                marudor.hafas.details("${currentTrip.trainType} ${currentTrip.trainNumber}")
            }
            val originText =
                scope.fetchMarudorUrlAsync(stops, details, 0)
            val destinationText =
                scope.fetchMarudorUrlAsync(stops, details, stops.lastIndex)

            respond {
                embed {
                    title = train.displayName
                    field(translate("commands.current_ride.train")) {
                        if (currentTrip.marudor != null) {
                            "[${currentTrip.trainType} ${currentTrip.trainNumber}](${currentTrip.marudor})"
                        } else {
                            "${currentTrip.trainType} ${currentTrip.trainNumber}"
                        }
                    }
                    field {
                        name = translate("commands.current_ride.origin")
                        value = originText.await()
                        inline = true
                    }
                    field {
                        value = Emojis.arrowRight.toString()
                        inline = true
                    }
                    field {
                        name = translate("commands.current_ride.destination")
                        value = destinationText.await()
                        inline = true
                    }
                    val departure = stops.first().formattedDeparture
                    val arrival = stops.last().formattedArrival
                    if (departure != null && arrival != null) {
                        field {
                            name = translate("commands.current_ride.departure")
                            value = departure
                            inline = true
                        }
                        field {
                            name = translate("commands.current_ride.arrival")
                            value = arrival
                            inline = true
                        }
                    }
                }
            }
        }
    }

private fun Collection<Trip>.findCurrentTripOrNull(): Trip? {
    return filterNot(Trip::isObsolete).minByOrNull { it.arrival!! }
}

private fun Trip.isObsolete(): Boolean {
    return arrival?.plus(30.minutes)?.let {
        it < Clock.System.now()
    } ?: true
}

private val Trip.arrival: Instant?
    get() = stops?.last()?.arrival

private val TrainVehicle.displayName: String
    get() {
        val override = enumValues<TrainOverride>().find { it.number == number }
        return override?.formatEmbedTitle(this) ?: "${Emojis.bullettrainSide} $name"
    }

private val Stop.formattedDeparture: String?
    get() = formattedTrainTime(departure, scheduledDeparture)

private val Stop.formattedArrival: String?
    get() = formattedTrainTime(arrival, scheduledArrival)

private fun formattedTrainTime(
    time: Instant?,
    scheduledTime: Instant?,
): String? {
    if (time == null || scheduledTime == null) {
        return null
    }
    val timeText = time.toMessageFormat(
        DiscordTimestampStyle.ShortTime
    )
    return if (time > scheduledTime) {
        "~~${scheduledTime.toMessageFormat(DiscordTimestampStyle.ShortTime)}~~ $timeText"
    } else {
        timeText
    }
}
