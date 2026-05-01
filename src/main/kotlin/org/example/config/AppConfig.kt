package org.example.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "raterr")
data class AppConfig(
    val tmdb: TmdbConfig = TmdbConfig()
) {
    data class TmdbConfig(
        val apiKey: String = ""
    )
}
