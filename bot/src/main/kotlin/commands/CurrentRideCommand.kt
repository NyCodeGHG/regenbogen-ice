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
import dev.nycode.regenbogenice.client.TrainVehicle
import dev.nycode.regenbogenice.train.TrainOverride
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.koin.core.component.inject

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

        action {
            val train = client.fetchTrainVehicle(
                arguments.name, 1,
                includeRoutes = true,
                includeMarudorLink = true
            )
            val currentTrip = train.trips?.firstOrNull() ?: discordError("No trips could be found.")
            val stops = currentTrip.stops ?: discordError("The fetched trip has no routes.")
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
                        value = stops.first().station
                        inline = true
                    }
                    field {
                        value = Emojis.arrowRight.toString()
                        inline = true
                    }
                    field {
                        name = translate("commands.current_ride.destination")
                        value = stops.last().station
                        inline = true
                    }
                    val departure = stops.first().departure
                    val arrival = stops.last().arrival
                    if (departure != null && arrival != null) {
                        field {
                            name = translate("commands.current_ride.departure")
                            value = departure.toMessageFormat(DiscordTimestampStyle.ShortTime)
                            inline = true
                        }
                        field {
                            name = translate("commands.current_ride.arrival")
                            value = arrival.toMessageFormat(DiscordTimestampStyle.ShortTime)
                            inline = true
                        }
                    }
                }
            }
        }
    }

private val TrainVehicle.displayName: String
    get() {
        val override = enumValues<TrainOverride>().find { it.number == number }
        return override?.embedTitle ?: "${Emojis.bullettrainSide} $name"
    }
