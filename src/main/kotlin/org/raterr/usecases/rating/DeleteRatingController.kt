package org.raterr.usecases.rating

import org.raterr.usecases.movie.MovieRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.NoSuchElementException

@Controller
class DeleteRatingController(
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository
) {

    @PostMapping("/top/delete/{id}")
    fun deleteRating(
        @PathVariable("id") tmdbId: Int,
        redirectAttributes: RedirectAttributes
    ): String {
        return try {
            val movie = movieRepository.findByIdWithRatings(tmdbId)
                .orElseThrow { NoSuchElementException("Movie not found") }

            val deletedCount = ratingRepository.deleteByMovieTmdbId(movie.tmdbId)

            if (deletedCount == 0) {
                throw NoSuchElementException("Rating not found")
            }

            redirectAttributes.addFlashAttribute("success", "Rating deleted successfully.")
            "redirect:/top"
        } catch (e: NoSuchElementException) {
            redirectAttributes.addFlashAttribute("error", "Could not delete the rating.")
            "redirect:/top"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Could not delete the rating.")
            "redirect:/top"
        }
    }
}