package com.cstr.unifieddrive.user.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant


@Document("users")
data class UserAcc (
    @Id
    val id: String? = null,
    @Indexed(unique = true)
    val email: String,
    val passHash: String,
    val roles: Set<Role> = setOf(Role.USER),
    @CreatedDate
    var createdAt: Instant? = null,
    @LastModifiedDate
    var updatedAt: Instant? = null
)