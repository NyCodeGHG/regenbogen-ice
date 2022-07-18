package dev.nycode.regenbogenice.presence

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.core.health.check.HealthCheck
import kotlinx.coroutines.isActive
import org.koin.core.component.inject
import org.pf4j.Extension

@Extension
class PresenceHealthCheck : HealthCheck, KordExKoinComponent {

    private val presence by inject<RailTrackPresence>()

    override suspend fun checkHealth(): Boolean {
        return presence.isActive
    }
}
