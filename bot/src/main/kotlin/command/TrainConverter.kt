package dev.nycode.regenbogenice.command

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.koin.KordExContext
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import dev.nycode.regenbogenice.train.autocomplete
import dev.nycode.regenbogenice.util.minByDistanceOrNull
import dev.schlaubi.hafalsch.rainbow_ice.RainbowICE
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import dev.schlaubi.mikbot.plugin.api.util.discordError

@Converter(
    "train",
    types = [ConverterType.SINGLE, ConverterType.OPTIONAL, ConverterType.DEFAULTING],
    builderBuildFunctionStatements = [
        autoCompleteCode
    ]
)
class TrainConverter(override var validator: Validator<TrainVehicle> = null) :
    TrainAutoCompletingArgument<TrainVehicle>(validator) {
    override val signatureTypeString: String = "converter.train.train"

    override suspend fun parseText(text: String, context: CommandContext): Boolean {
        parsed = fetchTrain(text) ?: discordError("converter.train.train_not_found")
        return true
    }
}

private val rainbow by KordExContext.get().inject<RainbowICE>()

internal suspend fun fetchTrain(
    query: String,
    tripLimit: Int = 20,
    includeRoutes: Boolean = true,
    includeMarudorLink: Boolean = true,
): TrainVehicle? {
    return if (query.startsWith(TRAIN_PREFIX)) {
        rainbow.fetchTrain(
            query.substringAfter(TRAIN_PREFIX),
            tripLimit,
            includeRoutes,
            includeMarudorLink
        )
    } else {
        rainbow.fetchTrain(
            autocomplete(query).minByDistanceOrNull(query) ?: return null,
            tripLimit,
            includeRoutes,
            includeMarudorLink
        )
    }
}
