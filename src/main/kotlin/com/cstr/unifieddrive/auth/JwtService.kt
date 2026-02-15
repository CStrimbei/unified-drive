package com.cstr.unifieddrive.auth

import com.cstr.unifieddrive.user.model.JwtUserInfo
import com.cstr.unifieddrive.user.model.Role
import com.cstr.unifieddrive.user.model.UserAcc
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.secret}")
    private val secret: String,
    @Value("\${jwt.expiration-seconds}")
    private val expirationSeconds: Long
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun generateToken(user: UserAcc): String{
        val now = Instant.now()
        val expiry = now.plusSeconds(expirationSeconds)

        return Jwts.builder()
            .subject(user.id)
            .claim("email", user.email)
            .claim("roles", user.roles.map { it.name })
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(key)
            .compact()
    }

    fun parseToken(token: String): JwtUserInfo{
        try{
            val jwt = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)

            val body = jwt.payload
            val userId = body.subject
                ?: throw JwtException("Missing subject (userId)")
            val email = body["email"] as? String
                ?: throw JwtException("Missing email claim")

            val roles: Set<Role> = when (val rolesClaim = body["roles"]) {
                is Collection<*> -> rolesClaim
                    .filterIsInstance<String>()
                    .map { Role.valueOf(it) }
                    .toSet()
                else -> emptySet()
            }

            return JwtUserInfo(userId = userId, email = email, roles = roles)
        } catch (ex: JwtException){
            throw ex
        } catch (ex: Exception) {
            throw JwtException("Invalid JWT", ex)
        }
    }
}