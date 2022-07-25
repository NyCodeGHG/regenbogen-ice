package dev.nycode.regenbogenice.presence

import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.nycode.regenbogenice.notification.SentNotification
import dev.nycode.regenbogenice.notification.buildNotificationMessage
import dev.nycode.regenbogenice.notification.checkNotification
import dev.nycode.regenbogenice.notification.notificationCollection
import dev.nycode.regenbogenice.sentry.sentryTransaction
import dev.nycode.regenbogenice.train.fetchCurrentTrip
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.`in`
import org.litote.kmongo.newId
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

private const val REGENBOGEN_ICE_TZN = "304"

class RailTrackPresence(private val kord: Kord, database: Database) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private val runningMutex = Mutex()

    val isRunning: Boolean
        get() = runningMutex.isLocked

    fun start() = launch {
        runningMutex.withLock {
            while (isActive) {
                val (_, trip) = fetchCurrentTrip(REGENBOGEN_ICE_TZN)
                    ?: continue
                kord.editPresence {
                    watching("${trip.displayName} to ${trip.destinationStation}")
                }
                checkNotifications()
                delay(30.seconds)
            }
        }
    }

    private val sentNotifications = database.getCollection<SentNotification>("sent_notifications")
    private suspend fun checkNotifications() =
        sentryTransaction("checkNotifications()", "notifications") {
            coroutineScope {
                val allNotifications = childTransaction("notifications", "Resolve notifications") {
                    checkNotification(notificationCollection.find().toList())
                }
                val semaphore = Semaphore(6)
                allNotifications.forEach { (user, notifications) ->
                    semaphore.withPermit {
                        launch {
                            childTransaction("notifications", "Resolve notification for user") {
                                val (days, embeds) = buildNotificationMessage(user, notifications)
                                    ?: return@launch
                                val existingNotification =
                                    sentNotifications.findOne(
                                        and(
                                            SentNotification::user eq user,
                                            SentNotification::days `in` days,
                                        )
                                    )
                                val channel = kord.getUser(user)?.getDmChannelOrNull() ?: return@launch
                                if (existingNotification != null) {
                                    channel.getMessageOrNull(existingNotification.messageId)?.edit {
                                        this.embeds = embeds.toMutableList()
                                    }
                                    sentNotifications.save(existingNotification.copy(days = days))
                                } else {
                                    val message = channel.createMessage {
                                        this.embeds.addAll(embeds)
                                    }
                                    sentNotifications.save(
                                        SentNotification(
                                            newId(),
                                            user,
                                            message.id,
                                            message.channelId,
                                            days
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
}

private val TrainVehicle.Trip.displayName: String
    get() = "$trainType $trainNumber"
