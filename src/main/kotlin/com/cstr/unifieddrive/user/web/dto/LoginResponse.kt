package com.cstr.unifieddrive.user.web.dto

data class LoginResponse(
    val userId: String,
    val jwt: String,
    val sessionToken: String,
    val sessionId: String
)