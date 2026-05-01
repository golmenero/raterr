package org.example.usecases.rating

import jakarta.persistence.*
import org.example.usecases.movie.Movie

@Entity
@Table(name = "rating")
data class Rating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    val movie: Movie,
    
    @Column(name = "direccion", nullable = false)
    val direccion: Double,
    
    @Column(name = "fotografia", nullable = false)
    val fotografia: Double,
    
    @Column(name = "actuacion", nullable = false)
    val actuacion: Double,
    
    @Column(name = "banda_sonora", nullable = false)
    val bandaSonora: Double,
    
    @Column(name = "guion", nullable = false)
    val guion: Double,
    
    @Column(name = "created_at_epoch_ms", nullable = false)
    val createdAtEpochMs: Long
)
