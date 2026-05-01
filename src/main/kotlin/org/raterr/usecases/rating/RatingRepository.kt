package org.raterr.usecases.rating

import org.raterr.usecases.movie.Movie
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RatingRepository : JpaRepository<Rating, Int> {
    fun findByMovie(movie: Movie): List<Rating>
    
    @Query("SELECT r FROM Rating r JOIN r.movie m WHERE m.tmdbId = :tmdbId")
    fun findByMovieTmdbId(tmdbId: Int): List<Rating>
    
    fun deleteByMovie(movie: Movie): Int
}
