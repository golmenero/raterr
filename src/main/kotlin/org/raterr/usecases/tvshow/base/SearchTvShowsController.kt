package org.raterr.usecases.tvshow.base

import org.raterr.TmdbClient
import org.raterr.TmdbTvShow
import org.raterr.usecases.tvshow.rating.TvRating
import org.raterr.usecases.tvshow.rating.TvRatingRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class SearchTvShowsController(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository
) {

    @GetMapping("/tv")
    fun searchPage(@RequestParam("q", required = false) query: String?, model: Model): String {
        if (!query.isNullOrBlank()) {
            val results = searchAndCacheTvShows(query)
            model.addAttribute("query", query)
            model.addAttribute("shows", results)
        }
        return "tv-index"
    }

    private fun searchAndCacheTvShows(query: String): List<SearchTvShowsResponse> {
        val tmdbShows = tmdbClient.searchTvShows(query).take(16)
        return tmdbShows.map { tmdbShow ->
            buildResponseFromTmdb(tmdbShow)
        }
    }

    private fun buildResponseFromTmdb(tmdbShow: TmdbTvShow): SearchTvShowsResponse {
        val show = tvShowRepository.findById(tmdbShow.id).orElse(null)
        val ratings = if (show != null) {
            tvRatingRepository.findByTvShow(show)
        } else {
            emptyList()
        }
        val stats = calculateStats(ratings)

        return SearchTvShowsResponse(
            tmdbId = tmdbShow.id,
            name = tmdbShow.name,
            overview = tmdbShow.overview,
            firstAirDate = tmdbShow.firstAirDate,
            firstAirYear = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
            posterPath = tmdbShow.posterPath,
            tmdbVoteAverage = tmdbShow.voteAverage,
            averageScore = stats.averageScore,
            ratingsCount = stats.ratingsCount
        )
    }

    private fun calculateStats(ratings: List<TvRating>): SearchTvShowsScoreStats {
        if (ratings.isEmpty()) {
            return SearchTvShowsScoreStats(averageScore = 0.0, ratingsCount = 0)
        }

        val avg = ratings.map { rating ->
            (rating.directing + rating.cinematography + rating.acting + rating.soundtrack + rating.screenplay) / 5.0
        }.average()

        return SearchTvShowsScoreStats(averageScore = avg, ratingsCount = ratings.size)
    }
}

data class SearchTvShowsScoreStats(
    val averageScore: Double,
    val ratingsCount: Int
)

data class SearchTvShowsResponse(
    val tmdbId: Int,
    val name: String,
    val overview: String?,
    val firstAirDate: String?,
    val firstAirYear: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
    val averageScore: Double,
    val ratingsCount: Int
)
