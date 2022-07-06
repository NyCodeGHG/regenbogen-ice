package dev.nycode.regenbogenice.client

import dev.nycode.regenbogenice.client.routes.RegenbogenICE
import dev.nycode.regenbogenice.client.routes.TrainNotFoundException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

public class RegenbogenICEClient {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json = json)
        }
        install(Resources)
        defaultRequest {
            url.takeFrom("https://regenbogen-ice.de/")
        }
    }

    public suspend fun fetchTrainVehicle(
        query: String,
        tripLimit: Int? = null,
        includeRoutes: Boolean? = null,
        includeMarudorLink: Boolean? = null,
    ): TrainVehicle {
        val response = client.get(
            RegenbogenICE.TrainVehicle(
                query,
                tripLimit,
                includeRoutes,
                includeMarudorLink
            )
        )
        return when (response.status) {
            HttpStatusCode.NotFound -> throw TrainNotFoundException(response.bodyAsText())
            HttpStatusCode.OK -> response.body()
            else -> error("Unexpected response code: ${response.status}.")
        }
    }

    public suspend fun autoComplete(
        searchTerm: String,
    ): List<String> =
        client.get(RegenbogenICE.AutoComplete(searchTerm)).body()
}
