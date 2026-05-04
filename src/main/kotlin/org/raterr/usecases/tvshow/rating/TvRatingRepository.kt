package org.raterr.usecases.tvshow.rating

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TvRatingRepository : CrudRepository<TvRating, Int> {
    fun findByTvShowTmdbId(tmdbId: Int): List<TvRating>
    fun findByTvShowTmdbIdAndUserUsername(tmdbId: Int, username: String): List<TvRating>
    fun findByUserUsername(username: String): List<TvRating>
    fun deleteByTvShowTmdbIdAndUserUsername(tmdbId: Int, username: String): Int
}
