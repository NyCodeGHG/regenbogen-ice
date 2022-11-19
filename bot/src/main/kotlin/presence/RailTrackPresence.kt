package dev.nycode.regenbogenice.presence

import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.nycode.regenbogenice.notification.SentNotification
import dev.nycode.regenbogenice.notification.buildNotificationMessage
import dev.nycode.regenbogenice.notification.checkNotification
import dev.nycode.regenbogenice.notification.notificationCollection
import dev.nycode.regenbogenice.train.fetchCurrentTrip
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.litote.kmongo.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

const val REGENBOGEN_ICE_TZN = "train:304"

class RailTrackPresence(private val kord: Kord, database: Database) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private val runningMutex = Mutex()

    val isRunning: Boolean
        get() = runningMutex.isLocked

    fun start() = launch {
        runningMutex.withLock {
            while (isActive) {
                val information = fetchCurrentTrip(REGENBOGEN_ICE_TZN)
                if (information != null) {
                    val (_, trip) = information
                    kord.editPresence {
                        watching("${trip.displayName} to ${trip.destinationStation}")
                    }
                } else {
                    kord.editPresence {
                        listening("Einfahrtgeräusche")
                    }
                }
                checkNotifications()
                delay(30.seconds)
            }
        }
    }

    private val sentNotifications = database.getCollection<SentNotification>("sent_notifications")
    private suspend fun checkNotifications() {
        coroutineScope {
            val allNotifications = checkNotification(notificationCollection.find().toList())
            val semaphore = Semaphore(6)
            allNotifications.forEach { (user, notifications) ->
                launch {
                    semaphore.withPermit {
                        val (days, embeds) = buildNotificationMessage(user, notifications)
                            ?: return@launch
                        val query = """{"${'$'}and": [{"user": ${user.value}}, {"days": {"${'$'}in": ${Json.encodeToString(days)}}}]}""".trimIndent()
                        val existingNotification = sentNotifications.findOne(query)
                        runCatching {
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
