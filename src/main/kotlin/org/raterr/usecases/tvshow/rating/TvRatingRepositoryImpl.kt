package org.raterr.usecases.tvshow.rating

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class TvRatingRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) {

    fun deleteByTvShowTmdbIdAndUserUsername(tmdbId: Int, username: String): Int {
        return jdbcTemplate.update(
            "DELETE FROM tv_ratings WHERE tv_show_tmdb_id = ? AND user_username = ?",
            tmdbId,
            username
        )
    }
}
