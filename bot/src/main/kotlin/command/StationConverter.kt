package dev.nycode.regenbogenice.command

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.koin.KordExContext
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.nycode.regenbogenice.station.asRainbow
import dev.nycode.regenbogenice.util.minByDistanceOrNull
import dev.nycode.regenbogenice.util.takeOptional
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.rainbow_ice.RainbowICE
import dev.schlaubi.hafalsch.rainbow_ice.entity.Station
import dev.schlaubi.mikbot.plugin.api.util.discordError

@Converter(
    "station",
    types = [ConverterType.SINGLE, ConverterType.OPTIONAL, ConverterType.DEFAULTING],
    builderBuildFunctionStatements = [
        autoCompleteCode,
    ],
    imports = [
        "com.kotlindiscord.kord.extensions.koin.KordExContext",
        "dev.schlaubi.hafalsch.marudor.Marudor",
        "dev.kord.core.entity.interaction.AutoCompleteInteraction"
    ]
)
class StationConverter(override var validator: Validator<Station> = null) :
    AutoCompletingArgument<Station>(validator) {
    override val signatureTypeString: String = "converter.station.station"

    override suspend fun parseText(text: String, context: CommandContext): Boolean {
        if (text.isBlank()) {
            discordError(context.translate("converter.station.empty_station"))
        }
        parsed = findStationByName(text) ?: discordError(
            context.translate(
                "converter.station.not_found",
                arrayOf(text)
            )
        )
        return true
    }

    override suspend fun AutoCompleteInteraction.onAutoComplete() {
        suggestString {
            for (station in searchStation(focusedOption.value, 25)) {
                stationChoice(station)
            }
        }
    }
}

private val rainbow by KordExContext.get().inject<RainbowICE>()
private val marudor by KordExContext.get().inject<Marudor>()

internal suspend fun findStationByName(text: String) = if (text.startsWith("eva:")) {
    marudor.stopPlace.byEva(text.substringAfter("eva:"))?.asRainbow()
} else {
    searchStation(text)
        .minByDistanceOrNull(text)
}

internal suspend fun searchStation(query: String, max: Int? = null): List<Station> =
    if (query.isBlank()) {
        // Thank you, GitHub Copilot for converting this into this format,
        // so I don't have to type it out by hand! 💞
        listOf(
            Station(8000105, "Frankfurt(Main)Hbf"),
            Station(8000191, "Karlsruhe Hbf"),
            Station(8000096, "Stuttgart Hbf"),
            Station(8002549, "Hamburg Hbf"),
            Station(8000152, "Hannover Hbf"),
            Station(8000085, "Düsseldorf Hbf"),
            Station(8000207, "Köln Hbf"),
            Station(8011160, "Berlin Hbf"),
            Station(8000244, "Mannheim Hbf"),
            Station(8000261, "München Hbf"),
            Station(8000284, "Nürnberg Hbf"),
            Station(8000170, "Ulm Hbf"),
        )
    } else {
        rainbow.stationSearch(query).takeOptional(max)
    }
