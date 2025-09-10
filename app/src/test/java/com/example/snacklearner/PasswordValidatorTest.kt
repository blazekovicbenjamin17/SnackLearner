package com.example.snacklearner

import org.junit.Assert.*
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun passwordTooShort_returnsFalse() {
        assertFalse(PasswordValidator.isValid("123"))
    }

    @Test
    fun passwordLongEnough_returnsTrue() {
        assertTrue(PasswordValidator.isValid("123456"))
    }

    @Test
    fun emptyPassword_returnsFalse() {
        assertFalse(PasswordValidator.isValid(""))
    }
}
