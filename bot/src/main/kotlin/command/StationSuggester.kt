package dev.nycode.regenbogenice.command

import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.schlaubi.hafalsch.rainbow_ice.entity.Station

@Suppress("NOTHING_TO_INLINE")
inline fun StringChoiceBuilder.stationChoice(station: Station) {
    choice(station.name, "eva:${station.evaNumber}")
}
