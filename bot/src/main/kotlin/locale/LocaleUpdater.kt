package dev.nycode.regenbogenice.locale

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val updatingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

fun SlashCommandContext<*, *>.updateLocaleAsync() = updatingScope.launch {
    userLocaleCollection.save(
        UserLocaleSetting(
            user.id,
            getLocale().toLanguageTag()
        )
    )
}
