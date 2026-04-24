package org.example.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(sqliteDbPath: String) {
        Database.connect(
            url = "jdbc:sqlite:$sqliteDbPath",
            driver = "org.sqlite.JDBC",
            setupConnection = { connection ->
                connection.createStatement().use { stmt ->
                    stmt.execute("PRAGMA foreign_keys=ON;")
                }
            }
        )

        transaction {
            SchemaUtils.create(Movies, Ratings)
        }
    }
}

