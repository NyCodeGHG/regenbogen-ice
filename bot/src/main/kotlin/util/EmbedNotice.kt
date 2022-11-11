package dev.nycode.regenbogenice.util

import dev.kord.rest.builder.message.EmbedBuilder

inline fun EmbedBuilder.dataNotice(translate: (String) -> String) {
    footer {
        text = translate("general.data_notice")
    }
}
