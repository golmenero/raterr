package org.raterr.usecases.tvshow.rating

import org.raterr.usecases.tvshow.base.TvShow
import org.raterr.usecases.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TvRatingRepository : JpaRepository<TvRating, Int> {
    fun findByTvShow(tvShow: TvShow): List<TvRating>
    fun findByTvShowAndUser(tvShow: TvShow, user: User): List<TvRating>
    
    @Query("SELECT r FROM TvRating r JOIN FETCH r.tvShow WHERE r.user = :user")
    fun findByUser(user: User): List<TvRating>
    
    @Query("SELECT r FROM TvRating r JOIN r.tvShow t WHERE t.tmdbId = :tmdbId")
    fun findByTvShowTmdbId(tmdbId: Int): List<TvRating>
    
    @Modifying
    @Query("DELETE FROM TvRating r WHERE r.tvShow.tmdbId = :tmdbId AND r.user.username = :username")
    fun deleteByTvShowTmdbIdAndUsername(tmdbId: Int, username: String): Int
}
