package com.cstr.unifieddrive.user.model

data class JwtUserInfo(
    val userId: String,
    val email: String,
    val roles: Set<Role>
)