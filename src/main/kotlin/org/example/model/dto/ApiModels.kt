package org.example.model.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

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
    @field:NotNull(message = "tmdbId es requerido")
    val tmdbId: Int,
    
    @field:NotNull(message = "direccion es requerido")
    @field:Min(value = 1, message = "direccion debe ser mayor o igual a 1")
    @field:Max(value = 10, message = "direccion debe ser menor o igual a 10")
    val direccion: Double,
    
    @field:NotNull(message = "fotografia es requerido")
    @field:Min(value = 1, message = "fotografia debe ser mayor o igual a 1")
    @field:Max(value = 10, message = "fotografia debe ser menor o igual a 10")
    val fotografia: Double,
    
    @field:NotNull(message = "actuacion es requerido")
    @field:Min(value = 1, message = "actuacion debe ser mayor o igual a 1")
    @field:Max(value = 10, message = "actuacion debe ser menor o igual a 10")
    val actuacion: Double,
    
    @field:NotNull(message = "bandaSonora es requerido")
    @field:Min(value = 1, message = "bandaSonora debe ser mayor o igual a 1")
    @field:Max(value = 10, message = "bandaSonora debe ser menor o igual a 10")
    val bandaSonora: Double,
    
    @field:NotNull(message = "guion es requerido")
    @field:Min(value = 1, message = "guion debe ser mayor o igual a 1")
    @field:Max(value = 10, message = "guion debe ser menor o igual a 10")
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
