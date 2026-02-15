package com.cstr.unifieddrive.user.service

import com.cstr.unifieddrive.user.exception.EmailAlreadyUsedException
import com.cstr.unifieddrive.user.exception.InvalidCredentialsException
import com.cstr.unifieddrive.user.model.Role
import com.cstr.unifieddrive.user.model.UserAcc
import com.cstr.unifieddrive.user.repo.UserRepo
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(private val userRepo: UserRepo, private val passwordEncoder: PasswordEncoder) {

    fun registerUser(email: String, rawPassword: String): UserAcc {
        if(userRepo.existsByEmail(email)){
            throw EmailAlreadyUsedException(email)
        }

        val user = UserAcc(
            id = UUID.randomUUID().toString(),
            email = email,
            passHash = passwordEncoder.encode(rawPassword),
            roles = setOf(Role.USER)
        )

        return userRepo.save(user)
    }

    fun findByEmail(email: String): UserAcc? = userRepo.findByEmail(email)

    fun authenticate(email: String, rawPassword: String): UserAcc{
        val user = userRepo.findByEmail(email)
            ?: throw InvalidCredentialsException()

        if(!passwordEncoder.matches(rawPassword, user.passHash)){
            throw InvalidCredentialsException()
        }

        return user
    }

}