package dev.nycode.regenbogenice

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.kord.core.on
import dev.nycode.regenbogenice.commands.aboutCommand
import dev.nycode.regenbogenice.commands.currentRideCommand
import dev.nycode.regenbogenice.notification.notificationCommand
import dev.nycode.regenbogenice.presence.RailTrackPresence
import dev.schlaubi.hafalsch.client.invoke
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.rainbow_ice.RainbowICE
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikbot.plugin.api.config.Config
import dev.schlaubi.mikbot.plugin.api.config.Environment
import dev.schlaubi.mikbot.plugin.api.util.AllShardsReadyEvent
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import kotlinx.coroutines.cancel
import org.koin.core.component.inject

@PluginMain
class RegenbogenICEPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {

    private val client = HttpClient(OkHttp) {
        install(HttpRequestRetry) {
            exponentialDelay()
            maxRetries = 20
            retryOnServerErrors(3)
        }
        if (Config.ENVIRONMENT == Environment.DEVELOPMENT) {
            install(Logging)
        }
    }

    private val rainbowICE = RainbowICE {
        httpClient = client
    }
    private val marudor = Marudor {
        httpClient = client
    }

    override suspend fun ExtensibleBotBuilder.apply() {
        hooks {
            afterKoinSetup {
                loadModule {
                    single { rainbowICE }
                    single { marudor }
                    single { RailTrackPresence(get(), get()) }
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
    private val presence by inject<RailTrackPresence>()

    override suspend fun setup() {
        currentRideCommand()
        notificationCommand()
        aboutCommand()
        kord.on<AllShardsReadyEvent> {
            presence.start()
        }
    }

    override suspend fun unload() {
        presence.cancel()
    }
}
