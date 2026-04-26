package org.example.model

data class ApiError(val message: String)

data class MovieDto(
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val releaseDate: String?,
    val releaseYear: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
    val averageScore: Double,
    val ratingsCount: Int
)

data class MovieSuggestionDto(
    val tmdbId: Int,
    val title: String,
    val releaseYear: Int?
)

data class RatingRequest(
    val tmdbId: Int,
    val direccion: Double,
    val fotografia: Double,
    val actuacion: Double,
    val bandaSonora: Double,
    val guion: Double
)

data class RatingResult(
    val tmdbId: Int,
    val averageScore: Double,
    val ratingsCount: Int
)


data class TopMovieDto(
    val tmdbId: Int,
    val title: String,
    val releaseYear: Int?,
    val posterPath: String?,
    val averageScore: Double,
    val ratingsCount: Int,
    val direccion: Double = 0.0,
    val fotografia: Double = 0.0,
    val actuacion: Double = 0.0,
    val bandaSonora: Double = 0.0,
    val guion: Double = 0.0
)

