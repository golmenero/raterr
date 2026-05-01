package org.raterr.security

import org.raterr.usecases.user.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User as SpringUser
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(usernameOrEmail: String): UserDetails {
        val user = userRepository.findById(usernameOrEmail)
            .or { userRepository.findByEmail(usernameOrEmail) }
            .orElseThrow { UsernameNotFoundException("User not found: $usernameOrEmail") }

        return SpringUser(
            user.username,
            user.passwordHash,
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
    }
}
