package org.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.example.config.AppConfig

@SpringBootApplication
@EnableConfigurationProperties(AppConfig::class)
class RaterrApplication

fun main(args: Array<String>) {
    runApplication<RaterrApplication>(*args)
}
