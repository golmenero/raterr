package org.raterr.usecases.movie.rating

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RatingRepository : CrudRepository<Rating, Long> {
    fun findByMovieId(movieId: Long): List<Rating>
    fun findByMovieIdAndUserId(movieId: Long, userId: Long): List<Rating>
    fun findByUserId(userId: Long): List<Rating>
    fun findAllWithoutUser(): List<Rating>
    fun deleteByMovieIdAndUserId(movieId: Long, userId: Long): Int
}
