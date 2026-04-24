package org.example

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.http.content.staticResources
import org.example.config.AppConfig
import org.example.db.DatabaseFactory
import org.example.model.ApiError
import org.example.model.RatingRequest
import org.example.service.RatingService
import org.example.service.TopService
import org.example.tmdb.TmdbClient

fun main() {
    val config = AppConfig.fromEnv()
    println("Iniciando Raterr en puerto ${config.port}...")
    
    embeddedServer(Netty, host = "0.0.0.0", port = config.port) {
        raterrModule(config)
    }.start(wait = true)
}

fun Application.raterrModule(config: AppConfig = AppConfig.fromEnv()) {
    install(ContentNegotiation) {
        jackson()
    }

    DatabaseFactory.init(config.sqliteDbPath)
    val tmdbClient = TmdbClient(config.tmdbApiKey)
    val ratingService = RatingService(tmdbClient)
    val topService = TopService()

    routing {
        // Statics
        staticResources("/", "static")

        // API
        get("/api/health") {
            call.respond(mapOf("status" to "ok"))
        }

        get("/api/search") {
            val query = call.request.queryParameters["q"].orEmpty()
            try {
                val results = ratingService.searchAndCacheMovies(query)
                call.respond(results)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Error")))
            }
        }

        get("/api/search/suggestions") {
            val query = call.request.queryParameters["q"].orEmpty()
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 5
            if (query.length < 2) {
                return@get call.respond(emptyList<Any>())
            }

            try {
                val suggestions = ratingService.searchSuggestions(query, limit)
                call.respond(suggestions)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiError(e.message ?: "Busqueda invalida"))
            } catch (_: Exception) {
                call.respond(HttpStatusCode.BadGateway, ApiError("No se pudo consultar TMDB"))
            }
        }

        get("/api/movie/{id}") {
            val tmdbId = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))
            try {
                val movie = ratingService.getMovieByTmdbId(tmdbId)
                call.respond(movie)
            } catch (_: Exception) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Película no encontrada"))
            }
        }

        delete("/api/movie/{id}/rating") {
            val tmdbId = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiError("ID invalido"))

            try {
                val result = ratingService.deleteRatingByTmdbId(tmdbId)
                call.respond(result)
            } catch (_: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, ApiError("Valoracion no encontrada"))
            }
        }

        post("/api/rate") {
            try {
                val request = call.receive<RatingRequest>()
                val result = ratingService.addRating(request)
                call.respond(result)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, ApiError(e.message ?: "La pelicula ya tiene valoracion"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Error al guardar")))
            }
        }

        get("/api/tops") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
            val year = call.request.queryParameters["year"]?.toIntOrNull()
            try {
                val tops = topService.getTop(limit, year)
                call.respond(tops)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Error")))
            }
        }
    }

    println("✓ Raterr listo en http://0.0.0.0:${config.port}")
}

