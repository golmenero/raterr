package org.raterr.usecases.user

import jakarta.persistence.*
import org.raterr.usecases.rating.Rating
import java.time.Instant

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @Column(name = "username", unique = true, nullable = false, length = 50)
    val username: String,

    @Column(name = "email", unique = true, nullable = false, length = 255)
    val email: String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ratings: MutableList<Rating> = mutableListOf()
)
