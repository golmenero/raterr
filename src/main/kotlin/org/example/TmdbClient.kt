package org.example

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class TmdbClient(
    @Value("\${raterr.tmdb.api-key}")
    private val apiKey: String,
    
    @Value("\${raterr.tmdb.base-url}")
    private val baseUrl: String = "https://api.themoviedb.org/3"
) {
    private val webClient: WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build()

    fun searchMovies(query: String): List<TmdbMovie> {
        if (query.isBlank()) return emptyList()
        requireApiKey()

        return webClient.get()
            .uri { builder ->
                builder.path("/search/movie")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "es-ES")
                    .queryParam("include_adult", false)
                    .queryParam("page", 1)
                    .queryParam("query", query)
                    .build()
            }
            .retrieve()
            .bodyToMono(TmdbSearchResponse::class.java)
            .block()
            ?.results
            ?: emptyList()
    }

    fun movieDetails(tmdbId: Int): TmdbMovie {
        requireApiKey()

        return webClient.get()
            .uri { builder ->
                builder.path("/movie/{id}")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "es-ES")
                    .build(tmdbId)
            }
            .retrieve()
            .bodyToMono(TmdbMovie::class.java)
            .block()
            ?: throw RuntimeException("No se pudo obtener detalles de la pelicula")
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
