package org.example.usecases.system.migration

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "migrations")
data class Migration(
    @Id
    @Column(name = "name", nullable = false, length = 255)
    val name: String,
    
    @Column(name = "executed", nullable = false)
    var executed: Int = 0,
    
    @Column(name = "executed_at")
    var executedAt: Instant? = null
)
