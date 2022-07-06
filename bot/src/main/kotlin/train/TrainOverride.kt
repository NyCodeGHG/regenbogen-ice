package dev.nycode.regenbogenice.train

import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis

enum class TrainOverride(val number: Int, val displayName: String, val emoji: DiscordEmoji) {
    RAINBOW_ICE(304, "Regenbogen ICE", Emojis.rainbow),
    GERMANY_ICE(9457, "Deutschland ICE", Emojis.flagDe),
    FEMALE_ICE(9046, "Female ICE", Emojis.femaleSign),
    EUROPE_ICE(4601, "Europa ICE", Emojis.flagEu);

    val embedTitle: String
        get() = "$emoji $displayName"
}
