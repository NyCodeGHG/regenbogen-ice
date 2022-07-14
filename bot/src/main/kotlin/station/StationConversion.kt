package dev.nycode.regenbogenice.station

import dev.schlaubi.hafalsch.marudor.entity.Station as MarudorStation
import dev.schlaubi.hafalsch.rainbow_ice.entity.Station as RainbowStation

fun MarudorStation.asRainbow(): RainbowStation = RainbowStation(eva.toInt(), name)
