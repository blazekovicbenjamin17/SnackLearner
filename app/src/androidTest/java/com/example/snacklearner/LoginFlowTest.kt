package com.example.snacklearner

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginFlowTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testFullLoginFlow_ShowsSearchScreen() {
        // Unos emaila i lozinke
        onView(withId(R.id.usernameEditText)).perform(typeText("filip.maglic@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.passwordEditText)).perform(typeText("Pipo2004"), closeSoftKeyboard())
        onView(withId(R.id.loginButton)).perform(click())

        // Pauza za Firebase autentifikaciju
        Thread.sleep(3000)

        // Provjera da se prikazuje SearchFragment (npr. TextView za test)
        onView(withId(R.id.testSearchFragmentLoaded)).check(matches(isDisplayed()))
    }
}
