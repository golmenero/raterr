package org.raterr.usecases.movie

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MovieRepository : JpaRepository<Movie, Int> {
    fun findByTmdbId(tmdbId: Int): Optional<Movie>
    
    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.ratings WHERE m.tmdbId = :tmdbId")
    fun findByTmdbIdWithRatings(tmdbId: Int): Optional<Movie>
    
    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.ratings")
    fun findAllWithRatings(): List<Movie>
    
    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.ratings WHERE m.title LIKE %:query%")
    fun searchByTitleWithRatings(query: String): List<Movie>
}
