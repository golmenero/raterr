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
class GetTvShowDetailsController(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository
) {

    @GetMapping("/tv/rate")
    fun ratePage(@RequestParam("id") tmdbId: Int, model: Model): String {
        try {
            val show = getTvShowByTmdbId(tmdbId)
            val ratings = tvRatingRepository.findByTvShowTmdbId(tmdbId)
            val alreadyRated = ratings.isNotEmpty()
            
            model.addAttribute("show", show)
            model.addAttribute("alreadyRated", alreadyRated)
            return "tv-rate"
        } catch (e: Exception) {
            model.addAttribute("error", "Could not load the TV show.")
            return "tv-rate"
        }
    }

    private fun getTvShowByTmdbId(tmdbId: Int): GetTvShowDetailsResponse {
        val localShow = tvShowRepository.findById(tmdbId).orElse(null)

        val show = localShow ?: run {
            val tmdbShow = tmdbClient.tvShowDetails(tmdbId)
            upsertTvShow(tmdbShow)
        }

        return buildResponse(show)
    }

    private fun upsertTvShow(tmdbShow: TmdbTvShow): TvShow {
        val existing = tvShowRepository.findById(tmdbShow.id).orElse(null)
        val genres = tmdbShow.genres.joinToString(",") { it.name }

        return if (existing != null) {
            val updated = existing.copy(
                name = tmdbShow.name,
                originalName = tmdbShow.originalName,
                overview = tmdbShow.overview,
                firstAirDate = tmdbShow.firstAirDate,
                firstAirYear = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbShow.posterPath,
                tmdbVoteAverage = tmdbShow.voteAverage,
                genres = genres
            )
            tvShowRepository.save(updated)
        } else {
            val newShow = TvShow(
                tmdbId = tmdbShow.id,
                name = tmdbShow.name,
                originalName = tmdbShow.originalName,
                overview = tmdbShow.overview,
                firstAirDate = tmdbShow.firstAirDate,
                firstAirYear = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbShow.posterPath,
                tmdbVoteAverage = tmdbShow.voteAverage,
                genres = genres
            )
            tvShowRepository.save(newShow)
        }
    }

    private fun buildResponse(show: TvShow): GetTvShowDetailsResponse {
        val ratings = tvRatingRepository.findByTvShow(show)
        val stats = calculateStats(ratings)

        return GetTvShowDetailsResponse(
            tmdbId = show.tmdbId,
            name = show.name,
            overview = show.overview,
            firstAirDate = show.firstAirDate,
            firstAirYear = show.firstAirYear,
            posterPath = show.posterPath,
            tmdbVoteAverage = show.tmdbVoteAverage,
            averageScore = stats.averageScore,
            ratingsCount = stats.ratingsCount
        )
    }

    private fun calculateStats(ratings: List<TvRating>): GetTvShowScoreStats {
        if (ratings.isEmpty()) {
            return GetTvShowScoreStats(averageScore = 0.0, ratingsCount = 0)
        }

        val avg = ratings.map { rating ->
            (rating.directing + rating.cinematography + rating.acting + rating.soundtrack + rating.screenplay) / 5.0
        }.average()

        return GetTvShowScoreStats(averageScore = avg, ratingsCount = ratings.size)
    }
}

private data class GetTvShowScoreStats(
    val averageScore: Double,
    val ratingsCount: Int
)

data class GetTvShowDetailsResponse(
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
