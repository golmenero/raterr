package org.raterr.usecases.search

import org.raterr.TmdbClient
import org.raterr.usecases.follow.FollowRepository
import org.raterr.usecases.movie.MovieRepository
import org.raterr.usecases.movie.rating.Rating
import org.raterr.usecases.movie.rating.RatingRepository
import org.raterr.usecases.tvshow.TvShowRepository
import org.raterr.usecases.tvshow.rating.TvRating
import org.raterr.usecases.tvshow.rating.TvRatingRepository
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class SearchController(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
    private val followRepository: FollowRepository
) {

    @GetMapping("/search")
    fun searchPage(@RequestParam("q", required = false) query: String?, model: Model, authentication: Authentication?): String {
        val currentUsername = authentication?.name
        if (!query.isNullOrBlank()) {
            val movies = searchMovies(query, currentUsername).take(6)
            val shows = searchTvShows(query, currentUsername).take(6)
            val interleaved = interleave(movies, shows)
            model.addAttribute("query", query)
            model.addAttribute("results", interleaved)
            model.addAttribute("currentUser", currentUsername)
        }
        return "search"
    }

    private fun interleave(movies: List<SearchResultItem>, shows: List<SearchResultItem>): List<SearchResultItem> {
        val result = mutableListOf<SearchResultItem>()
        val maxSize = maxOf(movies.size, shows.size)
        for (i in 0 until maxSize) {
            if (i < movies.size) result.add(movies[i])
            if (i < shows.size) result.add(shows[i])
        }
        return result
    }

    private fun searchMovies(query: String, currentUsername: String?): List<SearchResultItem> {
        return tmdbClient.searchMovies(query).map { tmdbMovie ->
            val movie = movieRepository.findById(tmdbMovie.id).orElse(null)
            val ratings = if (movie != null) ratingRepository.findByMovie(movie) else emptyList()
            val stats = calculateMovieStats(ratings)
            val isFollowed = currentUsername?.let {
                followRepository.existsByUserUsernameAndContentTypeAndContentTmdbId(it, "movie", tmdbMovie.id)
            } ?: false
            SearchResultItem(
                tmdbId = tmdbMovie.id,
                title = tmdbMovie.title,
                overview = tmdbMovie.overview,
                year = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath,
                tmdbVoteAverage = tmdbMovie.voteAverage,
                averageScore = stats.averageScore,
                ratingsCount = stats.ratingsCount,
                type = "movie",
                isFollowed = isFollowed
            )
        }
    }

    private fun searchTvShows(query: String, currentUsername: String?): List<SearchResultItem> {
        return tmdbClient.searchTvShows(query).map { tmdbShow ->
            val show = tvShowRepository.findById(tmdbShow.id).orElse(null)
            val ratings = if (show != null) tvRatingRepository.findByTvShow(show) else emptyList()
            val stats = calculateTvStats(ratings)
            val isFollowed = currentUsername?.let {
                followRepository.existsByUserUsernameAndContentTypeAndContentTmdbId(it, "tvshow", tmdbShow.id)
            } ?: false
            SearchResultItem(
                tmdbId = tmdbShow.id,
                title = tmdbShow.name,
                overview = tmdbShow.overview,
                year = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbShow.posterPath,
                tmdbVoteAverage = tmdbShow.voteAverage,
                averageScore = stats.averageScore,
                ratingsCount = stats.ratingsCount,
                type = "tvshow",
                isFollowed = isFollowed
            )
        }
    }

    private fun calculateMovieStats(ratings: List<Rating>): SearchScoreStats {
        if (ratings.isEmpty()) return SearchScoreStats(0.0, 0)
        val avg = ratings.map { (it.directing + it.cinematography + it.acting + it.soundtrack + it.screenplay) / 5.0 }.average()
        return SearchScoreStats(avg, ratings.size)
    }

    private fun calculateTvStats(ratings: List<TvRating>): SearchScoreStats {
        if (ratings.isEmpty()) return SearchScoreStats(0.0, 0)
        val avg = ratings.map { (it.directing + it.cinematography + it.acting + it.soundtrack + it.screenplay) / 5.0 }.average()
        return SearchScoreStats(avg, ratings.size)
    }
}

data class SearchScoreStats(
    val averageScore: Double,
    val ratingsCount: Int
)

data class SearchResultItem(
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val year: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
    val averageScore: Double,
    val ratingsCount: Int,
    val type: String,
    val isFollowed: Boolean = false
)
