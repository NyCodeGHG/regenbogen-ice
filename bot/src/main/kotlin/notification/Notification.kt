package dev.nycode.regenbogenice.notification

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class Notification(
    @SerialName("_id")
    @Contextual
    val id: Id<Notification> = newId(),
    val user: Snowflake,
    val train: Int,
    val station: Int,
)
