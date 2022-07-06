package dev.nycode.regenbogenice.commands

import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import dev.nycode.regenbogenice.RegenbogenICEExtension
import dev.nycode.regenbogenice.client.RegenbogenICEClient
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.koin.core.component.inject

suspend fun RegenbogenICEExtension.currentLocationCommand() = publicSlashCommand {
    name = "current-location"
    description = "Shows the current location of the Regenbogen ICE."

    val client by inject<RegenbogenICEClient>()

    action {
        val train = client.fetchTrainVehicle(
            "304", 1,
            includeRoutes = true,
            includeMarudorLink = true
        )
        val currentTrip = train.trips?.firstOrNull() ?: discordError("No trips could be found.")
        val stops = currentTrip.stops ?: discordError("The fetched trip has no routes.")
        respond {
            embed {
                title = "${Emojis.rainbow} Regenbogen ICE"
                field(translate("commands.current_location.train")) {
                    if (currentTrip.marudor != null) {
                        "[${currentTrip.trainType} ${currentTrip.trainNumber}](${currentTrip.marudor})"
                    } else {
                        "${currentTrip.trainType} ${currentTrip.trainNumber}"
                    }
                }
                field {
                    name = translate("commands.current_location.origin")
                    value = stops.first().station
                    inline = true
                }
                field {
                    value = Emojis.arrowRight.toString()
                    inline = true
                }
                field {
                    name = translate("commands.current_location.destination")
                    value = stops.last().station
                    inline = true
                }
                val departure = stops.first().departure
                val arrival = stops.last().arrival
                if (departure != null && arrival != null) {
                    field {
                        name = translate("commands.current_location.departure")
                        value = departure.toMessageFormat(DiscordTimestampStyle.ShortTime)
                    }
                    field {
                        name = translate("commands.current_location.arrival")
                        value = arrival.toMessageFormat(DiscordTimestampStyle.ShortTime)
                    }
                }
            }
        }
    }
}
