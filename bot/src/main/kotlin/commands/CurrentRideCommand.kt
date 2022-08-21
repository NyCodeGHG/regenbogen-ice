package dev.nycode.regenbogenice.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.Emojis
import dev.nycode.regenbogenice.RegenbogenICEExtension
import dev.nycode.regenbogenice.command.optionalTrainTrip
import dev.nycode.regenbogenice.locale.updateLocaleAsync
import dev.nycode.regenbogenice.presence.REGENBOGEN_ICE_TZN
import dev.nycode.regenbogenice.train.TrainOverride
import dev.nycode.regenbogenice.train.fetchCurrentTrip
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.JourneyInformation
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import dev.schlaubi.mikbot.plugin.api.util.discordError
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.datetime.Instant
import org.koin.core.component.inject
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle.Trip.Stop as RainbowStop

class CurrentLocationCommandArguments : Arguments(), KordExKoinComponent {
    val train by optionalTrainTrip {
        name = "commands.current_ride.argument.train.name"
        description = "commands.current_ride.argument.train.description"
    }
}

suspend fun RegenbogenICEExtension.currentRideCommand() =
    publicSlashCommand(::CurrentLocationCommandArguments) {
        name = "commands.current_ride.name"
        description = "commands.current_ride.description"

        val marudor by inject<Marudor>()
        val client = HttpClient(OkHttp) {
            followRedirects = false
        }

        fun CoroutineScope.fetchMarudorUrlAsync(
            stops: List<RainbowStop>,
            detailsDeferred: Deferred<JourneyInformation?>,
            index: Int,
        ) = async {
            val defaultText = stops[index].station
            val details = detailsDeferred.await() ?: return@async defaultText
            val id = details.stops[index].station.id

            val url = marudor.hafas.detailsRedirect(details.journeyId)
            val response = client.get(url)
            val marudorUrl = marudor.buildUrl {
                takeFrom(response.headers[HttpHeaders.Location]!!)
                pathSegments = encodedPathSegments // small hack to encode path
                parameters.append("stop", id)
            }
            "[$defaultText]($marudorUrl)"
        }

        action {
            updateLocaleAsync()
            val scope = CoroutineScope(Dispatchers.IO)
            val (train, currentTrip) = arguments.train ?: fetchCurrentTrip(REGENBOGEN_ICE_TZN)
            ?: discordError(translate("converter.train.no_trip_data"))
            val stops = /* just in case it's doch nullable */
                currentTrip.safeStops
            val response = interactionResponse.edit {
                buildMessage(
                    train,
                    currentTrip,
                    stops.first().station,
                    stops.last().station,
                    stops
                )
            }
            val details = scope.async {
                marudor.hafas.details("${currentTrip.trainType} ${currentTrip.trainNumber}")
            }
            val originText =
                scope.fetchMarudorUrlAsync(stops, details, 0)
            val destinationText = scope.fetchMarudorUrlAsync(
                stops,
                details,
                stops.lastIndex
            )
            response.edit {
                buildMessage(train, currentTrip, originText.await(), destinationText.await(), stops)
            }
        }
    }

context (InteractionResponseModifyBuilder)
        private suspend fun PublicSlashCommandContext<CurrentLocationCommandArguments>.buildMessage(
    train: TrainVehicle,
    currentTrip: TrainVehicle.Trip,
    originText: String,
    destinationText: String,
    stops: List<RainbowStop>,
) {
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
            value = originText
            inline = true
        }
        field {
            value = Emojis.arrowRight.toString()
            inline = true
        }
        field {
            name = translate("commands.current_ride.destination")
            value = destinationText
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

val TrainVehicle.displayName: String
    get() {
        val override = enumValues<TrainOverride>().find { it.number == number }
        return override?.formatEmbedTitle(this) ?: "${Emojis.bullettrainSide} $name"
    }

private val RainbowStop.formattedDeparture: String?
    get() = formatTrainTime(departure, scheduledDeparture)

private val RainbowStop.formattedArrival: String?
    get() = formatTrainTime(arrival, scheduledArrival)

fun formatTrainTime(
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
