package org.example.usecases.system.migration

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MigrationRepository : JpaRepository<Migration, String> {
    fun findByExecuted(executed: Int): List<Migration>
}
