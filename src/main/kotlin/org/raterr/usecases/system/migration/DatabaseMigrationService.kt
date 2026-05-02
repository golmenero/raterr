package org.raterr.usecases.system.migration

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant
import jakarta.persistence.EntityManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Service
@Order(1)
class DatabaseMigrationService(
    private val migrationRepository: MigrationRepository,
    private val resourceLoader: ResourceLoader,
    private val entityManager: EntityManager,
    private val transactionManager: PlatformTransactionManager
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(DatabaseMigrationService::class.java)
    private val migrationPath = "classpath:db/migration/"
    private val transactionTemplate = TransactionTemplate(transactionManager)

    override fun run(vararg args: String) {
        logger.info("Starting database migrations...")
        
        transactionTemplate.execute {
            registerAllMigrations()
            executePendingMigrations()
            null
        }
        
        logger.info("Database migrations completed")
    }

    private fun registerAllMigrations() {
        val migrations = loadAvailableMigrations()
        
        migrations.forEach { migrationName ->
            val existing = migrationRepository.findById(migrationName).orElse(null)
            if (existing == null) {
                logger.info("Registering migration: $migrationName")
                migrationRepository.save(Migration(name = migrationName, executed = 0))
            }
        }
    }

    private fun executePendingMigrations() {
        val pendingMigrations = migrationRepository.findByExecuted(0)

        if (pendingMigrations.isEmpty()) {
            logger.info("No pending migrations to execute")
            return
        }

        logger.info("Found ${pendingMigrations.size} pending migration(s)")

        pendingMigrations.forEach { migration ->
            logger.info("Executing migration: ${migration.name}")
            
            try {
                val script = loadMigrationScript(migration.name)
                executeSqlScript(script)
                
                migration.executed = 1
                migration.executedAt = Instant.now()
                migrationRepository.save(migration)
                
                logger.info("Migration ${migration.name} completed successfully")
            } catch (e: Exception) {
                logger.error("Migration ${migration.name} failed: ${e.message}")
                throw e
            }
        }
    }

    private fun loadAvailableMigrations(): List<String> {
        val resource = resourceLoader.getResource(migrationPath)
        
        if (!resource.exists()) {
            logger.info("No migration directory found at $migrationPath")
            return emptyList()
        }
        
        return try {
            val file = resource.file
            file.listFiles()
                ?.filter { it.name.endsWith(".sql") }
                ?.map { it.name }
                ?.sorted()
                ?: emptyList()
        } catch (e: Exception) {
            logger.info("Cannot access migration directory: ${e.message}")
            emptyList()
        }
    }

    private fun loadMigrationScript(filename: String): String {
        val resource = resourceLoader.getResource("$migrationPath$filename")
        val inputStream = resource.inputStream
        BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
            return reader.readText()
        }
    }

    private fun executeSqlScript(sql: String) {
        val statements = sql.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        statements.forEach { statement ->
            entityManager.createNativeQuery(statement).executeUpdate()
        }
    }
}
