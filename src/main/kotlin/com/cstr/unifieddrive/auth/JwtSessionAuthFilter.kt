package com.cstr.unifieddrive.auth

import com.cstr.unifieddrive.user.repo.UserSessionRepo
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant

@Component
class JwtSessionAuthFilter(private val jwtService: JwtService, private val sessionRepo: UserSessionRepo): OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/api/auth")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        val sessionId = request.getHeader("unified-session-id")
        val sessionToken = request.getHeader("unified-session-token")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")|| sessionId.isNullOrBlank() || sessionToken.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        val rawToken = authHeader.substringAfter("Bearer").trim()

        try {
            val jwtUser = jwtService.parseToken(rawToken)

            val sessionOpt = sessionRepo.findById(sessionId)
            if(sessionOpt.isEmpty){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired session")
                return
            }

            val session = sessionOpt.get()

            if (session.userId != jwtUser.userId || session.sessionToken != sessionToken) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid session")
                return
            }

            sessionRepo.save(session.copy(lastAccessAt = Instant.now()))

            val authorities = jwtUser.roles
                .map { SimpleGrantedAuthority("ROLE_${it.name}") }

            val authentication = UsernamePasswordAuthenticationToken(
                jwtUser.userId,
                null,
                authorities
            )

            SecurityContextHolder.getContext().authentication = authentication

            filterChain.doFilter(request, response)
        } catch (ex: Exception){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token")
        }
    }

}