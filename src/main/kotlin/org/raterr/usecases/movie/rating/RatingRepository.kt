package org.raterr.usecases.movie.rating

import org.raterr.usecases.movie.base.Movie
import org.raterr.usecases.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RatingRepository : JpaRepository<Rating, Int> {
    fun findByMovie(movie: Movie): List<Rating>
    fun findByMovieAndUser(movie: Movie, user: User): List<Rating>
    
    @Query("SELECT r FROM Rating r JOIN FETCH r.movie WHERE r.user = :user")
    fun findByUser(user: User): List<Rating>
    
    @Query("SELECT r FROM Rating r JOIN r.movie m WHERE m.tmdbId = :tmdbId")
    fun findByMovieTmdbId(tmdbId: Int): List<Rating>
    
    @Modifying
    @Query("DELETE FROM Rating r WHERE r.movie.tmdbId = :tmdbId AND r.user.username = :username")
    fun deleteByMovieTmdbIdAndUsername(tmdbId: Int, username: String): Int
}
