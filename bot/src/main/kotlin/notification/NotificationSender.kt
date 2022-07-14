package dev.nycode.regenbogenice.notification

import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.entity.Snowflake
import dev.kord.common.toMessageFormat
import dev.kord.core.Kord
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.Emojis
import dev.nycode.regenbogenice.commands.displayName
import dev.nycode.regenbogenice.commands.formatTrainTime
import dev.nycode.regenbogenice.locale.userLocaleCollection
import dev.nycode.regenbogenice.train.isObsolete
import dev.nycode.regenbogenice.util.isFuture
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import dev.schlaubi.mikbot.plugin.api.pluginSystem
import dev.schlaubi.mikbot.plugin.api.util.embed
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

suspend fun checkNotification(allNotifications: List<Notification>): Map<Snowflake, List<UserNotification>> {
    val cache = NotificationCache()
    return buildMap {
        for ((user, notifications) in allNotifications.groupBy { it.user }) {
            val userNotifications = buildList {
                for (notification in notifications) {
                    val train = cache.resolveTrain(notification.train)
                        ?: error("Unable to resolve train ${notification.train}")
                    val station = cache.resolveStation(notification.station)
                        ?: error("Unable to resolve station ${notification.station}")
                    for (trip in train.currentTrips) {
                        if (trip.stopsAt(station.name)) {
                            val stop = trip.findStop(station.name)!!
                            val actualTime = stop.actualTime
                            val plannedTime = stop.plannedTime
                            add(
                                UserNotification(
                                    train,
                                    trip,
                                    station.name,
                                    plannedTime,
                                    actualTime
                                )
                            )
                        }
                    }
                }
            }
            put(user, userNotifications)
        }
    }
}

private val TrainVehicle.Trip.Stop.actualTime
    get() = arrival ?: departure!!

private val TrainVehicle.Trip.Stop.plannedTime
    get() = scheduledArrival ?: scheduledDeparture!!

private val kord by KordExContext.get().inject<Kord>()

suspend fun buildNotificationMessage(
    userId: Snowflake,
    userNotification: List<UserNotification>
): List<EmbedBuilder>? {
    val user = kord.getUser(userId) ?: return null
    user.getDmChannelOrNull() ?: return null
    if (userNotification.isEmpty()) {
        return null
    }
    return userNotification.groupBy(UserNotification::train).map { (train, trainNotifications) ->
        embed {
            title = pluginSystem.translate(
                "notification.train_near_you",
                "regenbogen_ice",
                userLocaleCollection.findOneById(userId)?.locale,
                arrayOf(train.displayName)
            )
            description = buildString {
                for ((trip, tripNotifications) in trainNotifications.groupBy { it.trip }) {
                    val nextStop = trip.safeStops
                        .asSequence()
                        .filter { it.actualTime.isFuture }
                        .minBy { it.actualTime }
                    append(
                        tripNotifications.first().actualTime.toMessageFormat(
                            DiscordTimestampStyle.LongDate
                        )
                    )
                    appendLine()
                    appendLine()
                    append(trip.linkedDisplayName)
                    appendLine()
                    for (tripNotification in tripNotifications.sortedBy { it.plannedTime }) {
                        if (nextStop.station == tripNotification.station) {
                            append(Emojis.blueCircle)
                        } else {
                            append(Emojis.redCircle)
                        }
                        append(" - ")
                        append(
                            formatTrainTime(
                                tripNotification.actualTime,
                                tripNotification.plannedTime
                            )
                        )
                        append(" **")
                        append(tripNotification.station.strikethroughIf { tripNotification.actualTime < Clock.System.now() })
                        append("** ")
                        appendLine()
                    }
                    if (tripNotifications.none { it.station == nextStop.station }) {
                        appendLine()
                        append("Nächster Halt: ")
                        append(formatTrainTime(nextStop.arrival, nextStop.scheduledArrival))
                        appendLine()
                        append("**")
                        append(nextStop.station)
                        append("**")
                        appendLine()
                    }
                }
            }
        }
    }
}

inline fun String.strikethroughIf(condition: () -> Boolean): String {
    return if (condition()) {
        "~~$this~~"
    } else this
}

/**
 * Returns the display name of the trip,
 * formatted as a Markdown link, linking to marudor.de, if available.
 */
private val TrainVehicle.Trip.linkedDisplayName
    get() = buildString {
        if (marudor != null) {
            append('[')
        }
        append(trainType)
        append(' ')
        append(trainNumber)
        if (marudor != null) {
            append("](")
            append(marudor)
            append(')')
        }
    }

class UserNotification(
    val train: TrainVehicle,
    val trip: TrainVehicle.Trip,
    val station: String,
    val plannedTime: Instant,
    val actualTime: Instant
)

private val TrainVehicle.currentTrips
    get() = safeTrips.filterNot(TrainVehicle.Trip::isObsolete)

private fun TrainVehicle.Trip.stopsAt(station: String) = safeStops.any { it.station == station }

private fun TrainVehicle.Trip.findStop(station: String) = safeStops.find { it.station == station }
