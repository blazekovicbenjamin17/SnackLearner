package com.example.snacklearner

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginFlowTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private val idlingResource = CountingIdlingResource("FirebaseLoader")

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(idlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun login_withValidCredentials_opensSearchFragment() {
        onView(withId(R.id.usernameEditText))
            .perform(typeText("admin"), closeSoftKeyboard())

        onView(withId(R.id.passwordEditText))
            .perform(typeText("Admin2004"), closeSoftKeyboard())

        onView(withId(R.id.loginButton))
            .perform(click())

        Thread.sleep(2000)

        onView(withId(R.id.searchEditTextSearch))
            .check(matches(isDisplayed()))

        onView(withId(R.id.recyclerView))
            .check(matches(isDisplayed()))
    }
}
