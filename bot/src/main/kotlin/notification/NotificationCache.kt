package dev.nycode.regenbogenice.notification

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.nycode.regenbogenice.station.asRainbow
import dev.nycode.regenbogenice.sync.ReadWriteMutex
import dev.nycode.regenbogenice.sync.withReadLock
import dev.nycode.regenbogenice.sync.withWriteLock
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.rainbow_ice.RainbowICE
import dev.schlaubi.hafalsch.rainbow_ice.entity.Station
import dev.schlaubi.hafalsch.rainbow_ice.entity.TrainVehicle
import mu.KotlinLogging.logger
import org.koin.core.component.inject

class NotificationCache : KordExKoinComponent {

    private val marudor by inject<Marudor>()
    private val rainbow by inject<RainbowICE>()

    private val stations = mutableMapOf<Int, Station?>()
    private val stationsMutex = ReadWriteMutex()

    private val trains = mutableMapOf<Int, TrainVehicle?>()
    private val trainsMutex = ReadWriteMutex()

    private val logger = logger {}

    suspend fun resolveStation(eva: Int): Station? {
        stationsMutex.withReadLock {
            if (stations.containsKey(eva)) {
                val station = stations[eva]
                logger.debug { "Resolved station $eva (${station?.name}) from cache." }
                return station
            }
        }
        logger.debug { "Station $eva is not in cache." }
        stationsMutex.withWriteLock {
            logger.debug { "Fetching station $eva from marudor." }
            val station = marudor.stopPlace.byEva(eva.toString())?.asRainbow()
            logger.debug { "Resolved station $eva (${station?.name}) from marudor." }
            stations[eva] = station
            return station
        }
    }

    suspend fun resolveTrain(tzn: Int): TrainVehicle? {
        trainsMutex.withReadLock {
            if (trains.containsKey(tzn)) {
                val train = trains[tzn]
                logger.debug { "Resolved train $tzn (${train?.name}) from cache." }
                return train
            }
        }
        logger.debug { "Train $tzn is not in cache." }
        trainsMutex.withWriteLock {
            logger.debug { "Fetching train $tzn from regenbogen-ice." }
            val train = rainbow.fetchTrain(
                tzn.toString(),
                20,
                includeRoutes = true,
                includeMarudorLink = true
            )
            logger.debug { "Resolved train $tzn (${train?.name}) from regenbogen-ice." }
            trains[tzn] = train
            return train
        }
    }
}
