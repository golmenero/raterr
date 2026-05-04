package org.raterr.usecases.movie.rating

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("ratings")
data class Rating(
    @Id
    val id: Long? = null,
    val movieTmdbId: Int,
    val userUsername: String? = null,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val createdAtEpochMs: Long
)
