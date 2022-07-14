package dev.nycode.regenbogenice.train

import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle

enum class TrainOverride(val number: Int, val displayName: String?, val emoji: String) {
    RAINBOW_ICE(304, "Regenbogen ICE", Emojis.rainbow),
    GERMANY_ICE(9457, "Deutschland ICE", Emojis.flagDe),
    FEMALE_ICE(9046, "Female ICE", Emojis.femaleSign),
    EUROPE_ICE(4601, "Europa ICE", Emojis.flagEu),
    COLOGNE_ICE(4682, null, "<:cologne:994066157021704273>");

    constructor(number: Int, displayName: String?, emoji: DiscordEmoji) : this(
        number,
        displayName,
        emoji.toString()
    )

    fun formatEmbedTitle(vehicle: TrainVehicle): String {
        return "$emoji ${displayName ?: vehicle.name}"
    }
}
