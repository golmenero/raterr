package org.raterr.usecases.user

import org.raterr.security.CustomUserDetailsService
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class RegisterRequest(val username: String, val email: String, val password: String)
data class RegisterResponse(val username: String? = null, val message: String? = null)

@RestController
class RegisterController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userDetailsService: CustomUserDetailsService
) {

    @PostMapping("/api/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank()) {
            return ResponseEntity.badRequest()
                .body(RegisterResponse(message = "All fields are required"))
        }

        if (request.username.length < 3 || request.username.length > 50) {
            return ResponseEntity.badRequest()
                .body(RegisterResponse(message = "Username must be between 3 and 50 characters"))
        }

        if (request.password.length < 8) {
            return ResponseEntity.badRequest()
                .body(RegisterResponse(message = "Password must be at least 8 characters"))
        }

        if (userRepository.existsById(request.username)) {
            return ResponseEntity.badRequest()
                .body(RegisterResponse(message = "Username already exists"))
        }

        if (userRepository.existsByEmail(request.email)) {
            return ResponseEntity.badRequest()
                .body(RegisterResponse(message = "Email already exists"))
        }

        val hashedPassword = passwordEncoder.encode(request.password)

        val user = User(
            username = request.username,
            email = request.email,
            passwordHash = hashedPassword
        )

        userRepository.save(user)

        return ResponseEntity.ok(RegisterResponse(username = request.username))
    }
}
