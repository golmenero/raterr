package org.raterr.usecases.movie.rating

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class RatingRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) {

    fun findAllWithoutUser(): List<Rating> {
        return jdbcTemplate.query(
            "SELECT * FROM ratings WHERE user_username IS NULL",
            ratingRowMapper
        )
    }

    fun deleteByMovieTmdbIdAndUserUsername(tmdbId: Int, username: String): Int {
        return jdbcTemplate.update(
            "DELETE FROM ratings WHERE movie_tmdb_id = ? AND user_username = ?",
            tmdbId,
            username
        )
    }

    private val ratingRowMapper = { rs: java.sql.ResultSet, _: Int ->
        Rating(
            id = rs.getLong("id").takeIf { rs.wasNull() },
            movieTmdbId = rs.getInt("movie_tmdb_id"),
            userUsername = rs.getString("user_username"),
            directing = rs.getDouble("directing"),
            cinematography = rs.getDouble("cinematography"),
            acting = rs.getDouble("acting"),
            soundtrack = rs.getDouble("soundtrack"),
            screenplay = rs.getDouble("screenplay"),
            createdAtEpochMs = rs.getLong("created_at_epoch_ms")
        )
    }
}
