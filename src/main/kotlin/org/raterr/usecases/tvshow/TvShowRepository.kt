package org.raterr.usecases.tvshow

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TvShowRepository : JpaRepository<TvShow, Int> {
    @Query("SELECT t FROM TvShow t LEFT JOIN FETCH t.ratings WHERE t.tmdbId = :tmdbId")
    fun findByIdWithRatings(tmdbId: Int): Optional<TvShow>
    
    @Query("SELECT t FROM TvShow t LEFT JOIN FETCH t.ratings")
    fun findAllWithRatings(): List<TvShow>
    
    @Query("SELECT t FROM TvShow t LEFT JOIN FETCH t.ratings WHERE t.name LIKE %:query%")
    fun searchByNameWithRatings(query: String): List<TvShow>
}
