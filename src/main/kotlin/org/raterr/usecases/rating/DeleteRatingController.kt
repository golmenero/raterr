package org.raterr.usecases.rating

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.NoSuchElementException

@Controller
class DeleteRatingController(
    private val ratingRepository: RatingRepository
) {

    @PostMapping("/top/delete/{id}")
    @Transactional
    fun deleteRating(
        @PathVariable("id") tmdbId: Int,
        redirectAttributes: RedirectAttributes
    ): String {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            val username = authentication.name

            val deletedCount = ratingRepository.deleteByMovieTmdbIdAndUsername(tmdbId, username)

            if (deletedCount == 0) {
                throw NoSuchElementException("Rating not found")
            }

            redirectAttributes.addFlashAttribute("success", "Rating deleted successfully.")
            "redirect:/top"
        } catch (e: NoSuchElementException) {
            redirectAttributes.addFlashAttribute("error", "Could not delete the rating. A")
            "redirect:/top"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", e.printStackTrace())
            "redirect:/top"
        }
    }
}