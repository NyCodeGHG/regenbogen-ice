package dev.nycode.regenbogenice.train

import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.schlaubi.hafalsch.rainbow_ice.RainbowICE

private val rainbow by KordExContext.get().inject<RainbowICE>()

suspend fun autocomplete(query: String): List<String> {
    return if (query.isBlank()) {
        // TODO: Return popular trains
        emptyList()
    } else {
        rainbow.autocomplete(query)
    }
}
