package com.example.snacklearner

object PasswordValidator {
    fun isValid(password: String): Boolean {
        return password.length >= 6
    }
}
