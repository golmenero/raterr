package org.example.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "raterr")
data class AppConfig(
    var tmdb: TmdbConfig = TmdbConfig()
) {
    data class TmdbConfig(
        var apiKey: String = ""
    )
}
