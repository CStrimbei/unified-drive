package com.cstr.unifieddrive.user.repo

import com.cstr.unifieddrive.user.model.UserAcc
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepo : MongoRepository<UserAcc, String> {
    fun findByEmail(email: String): UserAcc?

    fun existsByEmail(email: String): Boolean
}