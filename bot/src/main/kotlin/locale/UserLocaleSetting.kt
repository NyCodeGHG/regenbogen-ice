package dev.nycode.regenbogenice.locale

import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.kord.common.entity.Snowflake
import dev.schlaubi.mikbot.plugin.api.io.Database
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val database by KordExContext.get().inject<Database>()

val userLocaleCollection = database.getCollection<UserLocaleSetting>("user_locales")

@Serializable
data class UserLocaleSetting(
    @SerialName("_id")
    val id: Snowflake,
    val locale: String
)
