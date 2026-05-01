package org.raterr.usecases.rating

import jakarta.persistence.*
import org.raterr.usecases.movie.Movie

@Entity
@Table(name = "ratings")
data class Rating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    val movie: Movie,
    
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
