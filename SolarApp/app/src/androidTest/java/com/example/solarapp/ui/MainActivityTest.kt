package com.example.solarapp.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.solarapp.R
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun shouldDisplayInitialViews() {
        onView(withId(R.id.editTextLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonSubmit)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonHere)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldNavigateToResultsWhenValidCityIsEntered() {
        val city = "Sao Paulo"
        
        try {
            onView(withText("OK")).perform(click())
        } catch (_: Exception) {
        }

        onView(withId(R.id.editTextLocation)).perform(replaceText(city), closeSoftKeyboard())
        onView(withId(R.id.buttonSubmit)).perform(click())

        Thread.sleep(4000)

        intended(
            allOf(
                hasComponent(ResultsActivity::class.java.name),
                hasExtra("city", city)
            )
        )
    }

    @Test
    fun shouldShowErrorDialogWhenInvalidCityIsEntered() {
        onView(withId(R.id.editTextLocation)).perform(replaceText("Sa0 Paulo"), closeSoftKeyboard())
        
        onView(withId(R.id.buttonSubmit)).perform(click())

        onView(withText(R.string.attention)).check(matches(isDisplayed()))
    }
}
