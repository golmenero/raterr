package org.raterr.usecases.follow

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FollowRepository : JpaRepository<Follow, Long> {
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
