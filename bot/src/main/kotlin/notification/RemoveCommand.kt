package dev.nycode.regenbogenice.notification

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import dev.nycode.regenbogenice.command.*
import dev.nycode.regenbogenice.locale.updateLocaleAsync
import dev.schlaubi.hafalsch.rainbow_ice.RainbowICE
import dev.schlaubi.stdx.coroutines.parallelMapNotNull
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.koin.core.component.inject
import org.litote.kmongo.coroutine.CoroutineCollection

internal class NotificationRemoveCommandArguments : Arguments(), KordExKoinComponent {
    private val rainbow by inject<RainbowICE>()

    val station by station {
        name = "commands.notification.remove.argument.station.name"
        description = "commands.notification.remove.argument.station.description"
        autoComplete {
            suggestString {
                val notifications =
                    notificationCollection.findByUser(user.id).map { it.station }.toList()
                val stations = searchStation(focusedOption.value)
                    .filter { it.evaNumber in notifications }
                for (station in stations) {
                    stationChoice(station)
                }
            }
        }
    }
    val train by train {
        name = "commands.notification.remove.argument.train.name"
        description = "commands.notification.remove.argument.train.description"
        autoComplete {
            val station = findStationByName(command.options["station"]!!.value.toString())
                ?: return@autoComplete
            suggestString {
                val trains =
                    notificationCollection.findByUser(user.id)
                        .filter { it.station == station.evaNumber }
                        .toList()
                        .parallelMapNotNull { rainbow.fetchTrain(it.train.toString()) }
                for (train in trains) {
                    trainChoice(train.name ?: train.number.toString())
                }
            }
        }
    }
}

internal suspend fun EphemeralSlashCommand<*>.removeCommand(
    collection: CoroutineCollection<Notification>,
) {
    ephemeralSubCommand(::NotificationRemoveCommandArguments) {
        name = "commands.notification.remove.name"
        description = "commands.notification.remove.description"

        action {
            updateLocaleAsync()
            val train = arguments.train
            val notification =
                collection.findNotification(user.id, train.number, arguments.station.evaNumber)
            if (notification == null) {
                respond {
                    embed {
                        title =
                            "${Emojis.x} ${translate("command.notification.remove.error.notification_not_exists.title")}"
                        description = translate(
                            "command.notification.remove.error.notification_not_exists.body",
                            arrayOf(arguments.station.name, train.name)
                        )
                        color = Color(0xff0000)
                    }
                }
                return@action
            }
            respond {
                embed {
                    title =
                        "${Emojis.whiteCheckMark} ${translate("command.notification.remove.success.title")}"
                    description = translate(
                        "command.notification.remove.success.body",
                        arrayOf(arguments.station.name, train.name)
                    )
                }
            }
            collection.deleteOneById(notification.id)
        }
    }
}
