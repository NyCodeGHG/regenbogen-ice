package dev.nycode.regenbogenice.command

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.nycode.regenbogenice.client.RegenbogenICEClient
import dev.nycode.regenbogenice.client.TrainVehicle
import dev.nycode.regenbogenice.client.Trip
import dev.nycode.regenbogenice.train.fetchCurrentTrip
import dev.schlaubi.mikbot.plugin.api.util.discordError
import org.koin.core.component.inject

private const val TRAIN_PREFIX = "train:"

data class TrainTripResult(val train: TrainVehicle, val trip: Trip)

@Converter(
    "train",
    types = [ConverterType.SINGLE, ConverterType.OPTIONAL, ConverterType.DEFAULTING],
    builderBuildFunctionStatements = [
        "autoComplete { with(converter) { onAutoComplete() } }"
    ]
)
class TrainTripConverter(
    override var validator: Validator<TrainTripResult> = null,
    private val tripLimit: Int = 20,
    private val includeRoutes: Boolean = true,
    private val includeMarudorLink: Boolean = true,
) : AutoCompletingArgument<TrainTripResult>(validator) {
    override val signatureTypeString: String = "converter.train.train"

    private val client by inject<RegenbogenICEClient>()

    override suspend fun parseText(text: String, context: CommandContext): Boolean {
        val train = if (text.startsWith(TRAIN_PREFIX)) {
            val train = text.substringAfter(TRAIN_PREFIX)
            client.fetchCurrentTrip(train, tripLimit, includeRoutes, includeMarudorLink)
        } else {
            client.fetchCurrentTrip(
                client.autoComplete(text).firstOrNull()
                    ?: discordError(
                        context.translate(
                            "converter.train.train_not_found",
                            arrayOf(text)
                        )
                    ),
                tripLimit,
                includeRoutes,
                includeMarudorLink
            )
        }
        parsed = train ?: discordError(context.translate("converter.train.no_trip_data"))
        return true
    }

    override suspend fun AutoCompleteInteraction.onAutoComplete() {
        val trains = client.autoComplete(focusedOption.value)
        suggestString {
            for (train in trains.take(25)) {
                choice(train, "$TRAIN_PREFIX$train")
            }
        }
    }
}
