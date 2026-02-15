package com.cstr.unifieddrive.user.web

import com.cstr.unifieddrive.auth.JwtService
import com.cstr.unifieddrive.user.exception.EmailAlreadyUsedException
import com.cstr.unifieddrive.user.exception.InvalidCredentialsException
import com.cstr.unifieddrive.user.model.UserSession
import com.cstr.unifieddrive.user.repo.UserSessionRepo
import com.cstr.unifieddrive.user.service.UserService
import com.cstr.unifieddrive.user.web.dto.*
import org.apache.coyote.Response
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val sessionRepo: UserSessionRepo
){
    @PostMapping("/register")
    fun register(@RequestBody body: RegisterUserRequest): ResponseEntity<UserResponse> {
        val user = userService.registerUser(
            email = body.email.trim(),
            rawPassword = body.password
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(user.toResponse())
    }

    @PostMapping("/login")
    fun login(@RequestBody body: LoginRequest): ResponseEntity<LoginResponse> {
        val user = userService.authenticate(
            email = body.email.trim(),
            rawPassword = body.password
        )

        val jwt = jwtService.generateToken(user)
        val sessionToken = UUID.randomUUID().toString()
        val now = Instant.now()

        val session = sessionRepo.save(
            UserSession(
                sessionToken = sessionToken,
                userId = user.id!!,
                lastAccessAt = now
            )
        )

        val response = LoginResponse(
            userId = user.id,
            jwt = jwt,
            sessionToken = sessionToken,
            sessionId = session.id!!
        )

        return ResponseEntity.ok(response)
    }

    @ExceptionHandler(EmailAlreadyUsedException::class)
    fun handleEmailAlreadyUsed(ex: EmailAlreadyUsedException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(mapOf("error" to ex.message.orEmpty()))
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(mapOf("error" to ex.message.orEmpty()))
    }
}