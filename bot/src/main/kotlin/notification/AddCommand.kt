package dev.nycode.regenbogenice.notification

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import dev.nycode.regenbogenice.command.optionalTrain
import dev.nycode.regenbogenice.command.station
import dev.schlaubi.hafalsch.rainbow_ice.RainbowICE
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.koin.core.component.inject
import org.litote.kmongo.coroutine.CoroutineCollection

internal class NotificationAddCommandArguments : Arguments(), KordExKoinComponent {
    val station by station {
        name = "commands.notification.add.argument.station.name"
        description = "commands.notification.add.argument.station.description"
    }
    val train by optionalTrain {
        name = "commands.notification.add.argument.train.name"
        description = "commands.notification.add.argument.train.description"
    }
}

internal suspend fun EphemeralSlashCommand<*>.addCommand(
    collection: CoroutineCollection<Notification>,
) {
    ephemeralSubCommand(::NotificationAddCommandArguments) {
        name = "commands.notification.add.name"
        description = "commands.notification.add.description"

        val rainbow by inject<RainbowICE>()

        action {
            val train =
                (arguments.train ?: rainbow.fetchTrain("304")) ?: discordError(
                    translate(
                        "converter.train.no_trip_data"
                    )
                )
            val notification =
                collection.findNotification(user.id, train.number, arguments.station.evaNumber)
            if (notification != null) {
                respond {
                    embed {
                        title =
                            "${Emojis.x} ${translate("command.notification.add.error.notification_exists.title")}"
                        description = translate(
                            "command.notification.add.error.notification_exists.body",
                            arrayOf(arguments.station.name, train.name)
                        )
                        color = Color(0xff0000)
                    }
                }
                return@action
            }
            val newNotification =
                Notification(
                    user = user.id,
                    train = train.number,
                    station = arguments.station.evaNumber
                )
            respond {
                embed {
                    title =
                        "${Emojis.whiteCheckMark} ${translate("command.notification.add.success.title")}"
                    description = translate(
                        "command.notification.add.success.body",
                        arrayOf(arguments.station.name, train.name)
                    )
                }
            }
            collection.save(newNotification)
        }
    }
}
