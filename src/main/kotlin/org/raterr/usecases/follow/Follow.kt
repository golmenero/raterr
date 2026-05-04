package org.raterr.usecases.follow

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("follows")
data class Follow(
    @Id
    val id: Long? = null,
    val userUsername: String,
    val contentType: String,
    val contentTmdbId: Int,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
