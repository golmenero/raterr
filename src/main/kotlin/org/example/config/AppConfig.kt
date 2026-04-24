package org.example.config

data class AppConfig(
    val port: Int,
    val tmdbApiKey: String,
    val sqliteDbPath: String
) {
    companion object {
        fun fromEnv(): AppConfig {
            val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
            val tmdbApiKey = System.getenv("TMDB_API_KEY").orEmpty()
            val sqliteDbPath = System.getenv("SQLITE_DB_PATH")?.trim().takeUnless { it.isNullOrEmpty() } ?: "raterr.db"
            return AppConfig(port = port, tmdbApiKey = tmdbApiKey, sqliteDbPath = sqliteDbPath)
        }
    }
}

