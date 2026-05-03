package org.raterr.usecases.follow

import jakarta.persistence.*
import org.raterr.usecases.user.User
import java.time.Instant

@Entity
@Table(name = "follows", uniqueConstraints = [
    UniqueConstraint(columnNames = ["user_username", "content_type", "content_tmdb_id"])
])
data class Follow(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_username", referencedColumnName = "username", nullable = false)
    val user: User,

    @Column(name = "content_type", nullable = false)
    val contentType: String,

    @Column(name = "content_tmdb_id", nullable = false)
    val contentTmdbId: Int,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
