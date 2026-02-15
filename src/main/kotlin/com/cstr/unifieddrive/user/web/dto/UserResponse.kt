package com.cstr.unifieddrive.user.web.dto

import com.cstr.unifieddrive.user.model.Role
import com.cstr.unifieddrive.user.model.UserAcc

data class UserResponse(
    val id: String,
    val email: String,
    val roles: Set<Role>
)

fun UserAcc.toResponse(): UserResponse = UserResponse(
    id = this.id!!,
    email = this.email,
    roles = this.roles
)