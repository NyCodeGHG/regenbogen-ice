package dev.nycode.regenbogenice.command

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.nycode.regenbogenice.train.fetchCurrentTrip
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import dev.schlaubi.mikbot.plugin.api.util.discordError

internal const val TRAIN_PREFIX = "train:"

data class TrainTripResult(val train: TrainVehicle, val trip: TrainVehicle.Trip)

@Converter(
    "trainTrip",
    types = [ConverterType.SINGLE, ConverterType.OPTIONAL, ConverterType.DEFAULTING],
    builderBuildFunctionStatements = [
        autoCompleteCode
    ]
)
class TrainTripConverter(
    override var validator: Validator<TrainTripResult> = null,
) : TrainAutoCompletingArgument<TrainTripResult>(validator) {
    override val signatureTypeString: String = "converter.train.train"

    override suspend fun parseText(text: String, context: CommandContext): Boolean {
        val train = fetchCurrentTrip(text) ?: discordError(
            context.translate(
                "converter.train.train_not_found",
                arrayOf(text)
            )
        )
        parsed = train
        return true
    }
}

fun StringChoiceBuilder.trainChoice(train: String) = choice(train, "$TRAIN_PREFIX$train")
