package org.raterr.usecases.rating

import org.raterr.usecases.movie.Movie
import org.raterr.usecases.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RatingRepository : JpaRepository<Rating, Int> {
    fun findByMovie(movie: Movie): List<Rating>
    fun findByMovieAndUser(movie: Movie, user: User): List<Rating>
    fun findByUser(user: User): List<Rating>
    
    @Query("SELECT r FROM Rating r JOIN r.movie m WHERE m.tmdbId = :tmdbId")
    fun findByMovieTmdbId(tmdbId: Int): List<Rating>
    
    @Query("DELETE FROM Rating r WHERE r.movie.tmdbId = :tmdbId AND r.user.username = :username")
    fun deleteByMovieTmdbIdAndUsername(tmdbId: Int, username: String): Int
}
