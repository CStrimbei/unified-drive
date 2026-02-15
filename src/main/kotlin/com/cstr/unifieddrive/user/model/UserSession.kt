package com.cstr.unifieddrive.user.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("sessions")
data class UserSession(
    @Id
    val id: String? = null,
    @Indexed(unique = true)
    val sessionToken: String,
    @Indexed
    val userId: String,
    @CreatedDate
    var createdAt: Instant? = null,
    val lastAccessAt: Instant
)