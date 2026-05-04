package org.raterr.usecases.movie.rating

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RatingRepository : CrudRepository<Rating, Int> {
    fun findByMovieTmdbId(tmdbId: Int): List<Rating>
    fun findByMovieTmdbIdAndUserUsername(tmdbId: Int, username: String): List<Rating>
    fun findByUserUsername(username: String): List<Rating>
    fun findAllWithoutUser(): List<Rating>
    fun deleteByMovieTmdbIdAndUserUsername(tmdbId: Int, username: String): Int
}
