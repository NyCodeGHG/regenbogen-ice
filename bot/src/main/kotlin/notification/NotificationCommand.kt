package dev.nycode.regenbogenice.notification

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.kord.common.entity.Snowflake
import dev.nycode.regenbogenice.RegenbogenICEExtension
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import kotlinx.coroutines.flow.Flow
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

val notificationCollection = KordExContext.get().get<Database>()
    .getCollection<Notification>("train_station_notifications")

suspend fun RegenbogenICEExtension.notificationCommand() = ephemeralSlashCommand {
    name = "commands.notification.name"
    description = "commands.notification.description"

    addCommand(notificationCollection)
    removeCommand(notificationCollection)
}

internal suspend fun CoroutineCollection<Notification>.findNotification(
    user: Snowflake,
    train: Int,
    station: Int,
): Notification? = findOne(
    and(
        Notification::user eq user,
        Notification::train eq train,
        Notification::station eq station
    )
)

internal fun CoroutineCollection<Notification>.findByUser(
    user: Snowflake,
    train: Int? = null,
): Flow<Notification> {
    val query = if (train != null) {
        and(Notification::user eq user, Notification::train eq train)
    } else {
        Notification::user eq user
    }
    return find(query).toFlow()
}
