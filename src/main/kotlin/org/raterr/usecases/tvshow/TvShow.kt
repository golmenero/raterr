package org.raterr.usecases.tvshow

import jakarta.persistence.*
import org.raterr.usecases.tvshow.rating.TvRating

@Entity
@Table(name = "tv_shows")
data class TvShow(
    @Id
    @Column(name = "tmdb_id")
    val tmdbId: Int,
    
    @Column(name = "name", nullable = false, length = 255)
    val name: String,
    
    @Column(name = "original_name", length = 255)
    val originalName: String? = null,
    
    @Column(name = "overview", columnDefinition = "TEXT")
    val overview: String? = null,
    
    @Column(name = "first_air_date", length = 20)
    val firstAirDate: String? = null,
    
    @Column(name = "first_air_year")
    val firstAirYear: Int? = null,
    
    @Column(name = "poster_path", length = 255)
    val posterPath: String? = null,
    
    @Column(name = "tmdb_vote_average")
    val tmdbVoteAverage: Double? = null,
    
    @Column(name = "genres", length = 500)
    val genres: String? = null,
    
    @OneToMany(mappedBy = "tvShow", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ratings: MutableList<TvRating> = mutableListOf()
)
