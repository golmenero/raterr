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
    val direccion: Int,
    val fotografia: Int,
    val actuacion: Int,
    val bandaSonora: Int,
    val guion: Int
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
    val ratingsCount: Int
)

