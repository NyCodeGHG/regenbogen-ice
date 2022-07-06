package dev.nycode.regenbogenice

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.nycode.regenbogenice.client.RegenbogenICEClient
import dev.nycode.regenbogenice.commands.currentRideCommand
import dev.schlaubi.hafalsch.client.invoke
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper

@PluginMain
class RegenbogenICEPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {

    private val client = RegenbogenICEClient()
    private val marudor = Marudor()

    override suspend fun ExtensibleBotBuilder.apply() {
        hooks {
            afterKoinSetup {
                loadModule {
                    single { client }
                    single { marudor }
                }
            }
        }
    }

    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::RegenbogenICEExtension)
    }
}

class RegenbogenICEExtension : Extension() {
    override val name: String = "Regenbogen ICE"
    override val bundle: String = "regenbogen_ice"

    override suspend fun setup() {
        currentRideCommand()
    }
}
