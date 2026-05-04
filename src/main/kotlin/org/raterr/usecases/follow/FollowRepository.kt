package org.raterr.usecases.follow

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FollowRepository : CrudRepository<Follow, Long> {
    fun findByUserUsernameAndContentTypeAndContentTmdbId(
        username: String,
        contentType: String,
        contentTmdbId: Int
    ): Optional<Follow>

    fun existsByUserUsernameAndContentTypeAndContentTmdbId(
        username: String,
        contentType: String,
        contentTmdbId: Int
    ): Boolean
}
