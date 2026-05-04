package org.raterr.usecases.tvshow.rating

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TvRatingRepository : CrudRepository<TvRating, Long> {
    fun findByTvShowId(tvShowId: Long): List<TvRating>
    fun findByTvShowIdAndUserId(tvShowId: Long, userId: Long): List<TvRating>
    fun findByUserId(userId: Long): List<TvRating>
    fun deleteByTvShowIdAndUserId(tvShowId: Long, userId: Long): Int
}
