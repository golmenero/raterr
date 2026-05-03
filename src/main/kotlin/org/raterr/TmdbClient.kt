package org.raterr

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
                    .queryParam("language", "en-US")
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
                    .queryParam("language", "en-US")
                    .build(tmdbId)
            }
            .retrieve()
            .bodyToMono(TmdbMovie::class.java)
            .block()
            ?: throw RuntimeException("Could not fetch movie details")
    }

    fun searchTvShows(query: String): List<TmdbTvShow> {
        if (query.isBlank()) return emptyList()
        requireApiKey()

        return webClient.get()
            .uri { builder ->
                builder.path("/search/tv")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "en-US")
                    .queryParam("include_adult", false)
                    .queryParam("page", 1)
                    .queryParam("query", query)
                    .build()
            }
            .retrieve()
            .bodyToMono(TmdbTvShowSearchResponse::class.java)
            .block()
            ?.results
            ?: emptyList()
    }

    fun tvShowDetails(tmdbId: Int): TmdbTvShow {
        requireApiKey()

        return webClient.get()
            .uri { builder ->
                builder.path("/tv/{id}")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "en-US")
                    .build(tmdbId)
            }
            .retrieve()
            .bodyToMono(TmdbTvShow::class.java)
            .block()
            ?: throw RuntimeException("Could not fetch TV show details")
    }

    private fun requireApiKey() {
        require(apiKey.isNotBlank()) {
            "TMDB_API_KEY is missing. Set the environment variable before using search or details."
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
    val voteAverage: Double? = null,
    @JsonProperty("genres")
    val genres: List<TmdbGenre> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTvShowSearchResponse(
    @JsonProperty("results")
    val results: List<TmdbTvShow> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTvShow(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("original_name")
    val originalName: String? = null,
    @JsonProperty("overview")
    val overview: String? = null,
    @JsonProperty("first_air_date")
    val firstAirDate: String? = null,
    @JsonProperty("poster_path")
    val posterPath: String? = null,
    @JsonProperty("vote_average")
    val voteAverage: Double? = null,
    @JsonProperty("genres")
    val genres: List<TmdbGenre> = emptyList(),
    @JsonProperty("status")
    val status: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbGenre(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("name")
    val name: String
)
