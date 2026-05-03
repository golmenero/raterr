package org.raterr.usecases.tvshow.rating

import jakarta.persistence.*
import org.raterr.usecases.tvshow.TvShow
import org.raterr.usecases.user.User

@Entity
@Table(name = "tv_ratings")
data class TvRating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tmdb_id", referencedColumnName = "tmdb_id", nullable = false)
    val tvShow: TvShow,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username")
    val user: User? = null,
    
    @Column(name = "directing", nullable = false)
    val directing: Double,
    
    @Column(name = "cinematography", nullable = false)
    val cinematography: Double,
    
    @Column(name = "acting", nullable = false)
    val acting: Double,
    
    @Column(name = "soundtrack", nullable = false)
    val soundtrack: Double,
    
    @Column(name = "screenplay", nullable = false)
    val screenplay: Double,
    
    @Column(name = "created_at_epoch_ms", nullable = false)
    val createdAtEpochMs: Long
)
