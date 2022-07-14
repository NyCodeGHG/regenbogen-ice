package dev.nycode.regenbogenice.notification

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
data class SentNotification(
    @SerialName("_id")
    @Contextual
    val id: Id<SentNotification>,
    val user: Snowflake,
    val tripIds: List<String>,
    val messageId: Snowflake,
    val channelId: Snowflake
)
