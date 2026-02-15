package com.cstr.unifieddrive.user.repo

import com.cstr.unifieddrive.user.model.UserSession
import org.springframework.data.mongodb.repository.MongoRepository

interface UserSessionRepo: MongoRepository<UserSession, String> {
    fun findBySessionToken(sessionToken: String): UserSession?
    fun deleteBySessionToken(sessionToken: String)
}