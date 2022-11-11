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
import dev.nycode.regenbogenice.util.dataNotice
import dev.nycode.regenbogenice.util.isFuture
import dev.nycode.regenbogenice.util.isPast
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import dev.schlaubi.mikbot.plugin.api.pluginSystem
import dev.schlaubi.mikbot.plugin.api.util.embed
import kotlinx.datetime.*
import org.jetbrains.annotations.PropertyKey

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

@JvmInline
value class UserContext(val userId: Snowflake)

@Suppress("UNCHECKED_CAST")
private suspend fun UserContext.translate(
    @PropertyKey(resourceBundle = "translations.regenbogen_ice.strings") key: String,
    vararg args: String = arrayOf()
) = pluginSystem.translate(
    key,
    "regenbogen_ice",
    userLocaleCollection.findOneById(userId)?.locale,
    args as Array<Any?>
)

suspend fun buildNotificationMessage(
    userId: Snowflake,
    userNotification: List<UserNotification>
): Pair<List<LocalDate>, List<EmbedBuilder>>? {
    val user = kord.getUser(userId) ?: return null
    user.getDmChannelOrNull() ?: return null
    if (userNotification.isEmpty()) {
        return null
    }
    with(UserContext(user.id)) {
        return userNotification.map { it.trip.firstStopDate }
            .distinct() to userNotification.groupBy(
            UserNotification::train
        ).map { (train, trainNotifications) ->
            embed {
                title = translate("notification.train_near_you", train.displayName)
                description = buildString {
                    trainNotifications.groupBy { it.trip }.toList()
                        .groupBy { it.first.firstStopDate }
                        .forEach { (date, tripData) ->
                            append(
                                date.atStartOfDayIn(TimeZone.UTC).toMessageFormat(
                                    DiscordTimestampStyle.LongDate
                                )
                            )
                            appendLine()
                            for ((trip, tripNotifications) in tripData.sortedBy { it.first.safeStops.first().actualTime }) {
                                appendLine()
                                val nextStop = trip.nextStation
                                append(trip.linkedDisplayName)
                                append(' ')
                                append(Emojis.pushpin)
                                append(' ')
                                append(trip.safeStops.last().station)
                                appendLine()
                                for (tripNotification in tripNotifications.sortedBy { it.plannedTime }) {
                                    if (nextStop?.station == tripNotification.station) {
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
                                    append(tripNotification.station.strikethroughIf { tripNotification.actualTime.isPast })
                                    append("** ")
                                    appendLine()
                                }
                                if (trip.isActive && tripNotifications.none { it.station == nextStop?.station }) {
                                    appendLine()
                                    append(translate("notification.next_stop"))
                                    append(' ')
                                    append(
                                        formatTrainTime(
                                            nextStop?.actualTime,
                                            nextStop?.plannedTime
                                        )
                                    )
                                    appendLine()
                                    append("**")
                                    append(nextStop?.station)
                                    append("**")
                                    appendLine()
                                }
                            }
                        }
                }
                dataNotice {
                    translate(it)
                }
            }
        }
    }
}

private val TrainVehicle.Trip.firstStopDate: LocalDate
    get() = safeStops.first().plannedTime.toLocalDateTime(TimeZone.UTC).date

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

private val TrainVehicle.Trip.isActive
    get() = safeStops.first().plannedTime.isPast && safeStops.last().plannedTime.isFuture

private val TrainVehicle.Trip.nextStation: TrainVehicle.Trip.Stop?
    get() = if (isActive) {
        safeStops
            .asSequence()
            .filter { it.actualTime.isFuture }
            .minBy { it.actualTime }
    } else null

private fun TrainVehicle.Trip.stopsAt(station: String) = safeStops.any { it.station == station }

private fun TrainVehicle.Trip.findStop(station: String) = safeStops.find { it.station == station }
