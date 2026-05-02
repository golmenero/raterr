package org.raterr.usecases.movie

import jakarta.persistence.*
import org.raterr.usecases.rating.Rating

@Entity
@Table(name = "movies")
data class Movie(
    @Id
    @Column(name = "tmdb_id")
    val tmdbId: Int,
    
    @Column(name = "title", nullable = false, length = 255)
    val title: String,
    
    @Column(name = "original_title", length = 255)
    val originalTitle: String? = null,
    
    @Column(name = "overview", columnDefinition = "TEXT")
    val overview: String? = null,
    
    @Column(name = "release_date", length = 20)
    val releaseDate: String? = null,
    
    @Column(name = "release_year")
    val releaseYear: Int? = null,
    
    @Column(name = "poster_path", length = 255)
    val posterPath: String? = null,
    
    @Column(name = "tmdb_vote_average")
    val tmdbVoteAverage: Double? = null,
    
    @Column(name = "genres", length = 500)
    val genres: String? = null,
    
    @OneToMany(mappedBy = "movie", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ratings: MutableList<Rating> = mutableListOf()
)
