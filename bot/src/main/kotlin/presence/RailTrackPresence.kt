package dev.nycode.regenbogenice.presence

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.nycode.regenbogenice.notification.*
import dev.nycode.regenbogenice.train.fetchCurrentTrip
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.inject
import org.litote.kmongo.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

private const val REGENBOGEN_ICE_TZN = "304"

class RailTrackPresence : CoroutineScope, KordExKoinComponent {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private val kord by inject<Kord>()
    private val isRunning = Mutex()

    fun start() = launch {
        isRunning.withLock {
            while (isActive) {
                val (_, trip) = fetchCurrentTrip(REGENBOGEN_ICE_TZN)
                    ?: continue
                kord.editPresence {
                    watching("${trip.displayName} to ${trip.destinationStation}")
                }
                launch {
                    checkNotifications()
                }
                delay(30.seconds)
            }
        }
    }

    private val sentNotifications = database.getCollection<SentNotification>("sent_notifications")
    private suspend fun checkNotifications() {
        val allNotifications = checkNotification(notificationCollection.find().toList())
        for ((user, notifications) in allNotifications) {
            val embeds = buildNotificationMessage(user, notifications) ?: continue
            val tripIds = notifications.map { it.trip.uniqueId }
            val existingNotification =
                sentNotifications.findOne(
                    and(
                        SentNotification::user eq user,
                        SentNotification::tripIds `in` tripIds
                    )
                )
            val channel = kord.getUser(user)?.getDmChannelOrNull() ?: return
            if (existingNotification != null) {
                channel.getMessageOrNull(existingNotification.messageId)?.edit {
                    this.embeds = embeds.toMutableList()
                }
            } else {
                val message = channel.createMessage {
                    this.embeds.addAll(embeds)
                }
                sentNotifications.save(
                    SentNotification(
                        newId(),
                        user,
                        tripIds,
                        message.id,
                        message.channelId
                    )
                )
            }
        }
    }
}

private val TrainVehicle.Trip.uniqueId: String
    get() = "$displayName $tripTimestamp"

private val TrainVehicle.Trip.displayName: String
    get() = "$trainType $trainNumber"
