package dev.nycode.regenbogenice.commands

import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import dev.nycode.regenbogenice.RegenbogenICEExtension

suspend fun RegenbogenICEExtension.aboutCommand() = publicSlashCommand {
    name = "commands.about"
    description = "commands.about.description"

    action {
        respond {
            embed {
                title = "Regenbogen ICE"
                description = translate("commands.about.text")
                field(translate("source-code")) {
                    "https://github.com/mikbot/regenbogen-ice"
                }
            }
        }
    }
}
