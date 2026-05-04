package org.raterr.usecases.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class User(
    @Id
    val username: String,
    val email: String,
    val passwordHash: String,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
