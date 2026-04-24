package org.example.tmdb

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.jackson.jackson

class TmdbClient(private val apiKey: String) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }

    suspend fun searchMovies(query: String): List<TmdbMovie> {
        if (query.isBlank()) return emptyList()
        requireApiKey()

        val response = client.get("https://api.themoviedb.org/3/search/movie") {
            parameter("api_key", apiKey)
            parameter("language", "es-ES")
            parameter("include_adult", false)
            parameter("page", 1)
            parameter("query", query)
        }.body<TmdbSearchResponse>()

        return response.results
    }

    suspend fun movieDetails(tmdbId: Int): TmdbMovie {
        requireApiKey()

        return client.get("https://api.themoviedb.org/3/movie/$tmdbId") {
            parameter("api_key", apiKey)
            parameter("language", "es-ES")
        }.body()
    }

    fun close() {
        client.close()
    }

    private fun requireApiKey() {
        require(apiKey.isNotBlank()) {
            "Falta TMDB_API_KEY. Configura la variable de entorno antes de usar busquedas o detalles de peliculas."
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbSearchResponse(
    @JsonProperty("results")
    val results: List<TmdbMovie> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbMovie(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("original_title")
    val originalTitle: String? = null,
    @JsonProperty("overview")
    val overview: String? = null,
    @JsonProperty("release_date")
    val releaseDate: String? = null,
    @JsonProperty("poster_path")
    val posterPath: String? = null,
    @JsonProperty("vote_average")
    val voteAverage: Double? = null
)

