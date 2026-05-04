package org.raterr.usecases.tvshow.rating

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("tv_ratings")
data class TvRating(
    @Id
    val id: Long? = null,
    val tvShowTmdbId: Int,
    val userUsername: String? = null,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val createdAtEpochMs: Long
)
