package org.raterr.usecases.tvshow

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TvShowRepository : CrudRepository<TvShow, Int>
