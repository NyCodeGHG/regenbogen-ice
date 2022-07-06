package dev.nycode.regenbogenice.client.routes

import io.ktor.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Resource("api")
public class RegenbogenICE {
    @Serializable
    @Resource("train_vehicle")
    public data class TrainVehicle(
        @SerialName("q")
        val query: String,
        @SerialName("trip_limit")
        val tripLimit: Int? = null,
        @SerialName("include_routes")
        val includeRoutes: Boolean? = null,
        @SerialName("include_marudor_link")
        val includeMarudorLink: Boolean? = null,
        val regenbogenICE: RegenbogenICE = RegenbogenICE(),
    )
}
