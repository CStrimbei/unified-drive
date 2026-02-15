package com.cstr.unifieddrive.user.exception

class EmailAlreadyUsedException(email: String): RuntimeException("Email is already in use: $email")