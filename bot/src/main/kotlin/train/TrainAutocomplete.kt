package dev.nycode.regenbogenice.train

import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.schlaubi.hafalsch.rainbow_ice.RainbowICE
import dev.schlaubi.stdx.coroutines.parallelMap

private val rainbow by KordExContext.get().inject<RainbowICE>()

suspend fun autocomplete(query: String): List<String> {
    return if (query.isBlank()) {
        enumValues<TrainOverride>().toList()
            .parallelMap { rainbow.fetchTrain(it.number.toString()) }.mapNotNull { it?.name }
    } else {
        rainbow.autocomplete(query)
    }
}
