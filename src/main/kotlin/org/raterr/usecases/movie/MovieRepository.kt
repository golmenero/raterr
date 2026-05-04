package org.raterr.usecases.movie

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MovieRepository : CrudRepository<Movie, Int>
