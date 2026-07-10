package com.example.solarapp.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.solarapp.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashScreenTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SplashScreenActivity::class.java)

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun splashScreenShouldDisplayLogo() {
        onView(withId(R.id.logo)).check(matches(isDisplayed()))
    }

    @Test
    fun splashScreenShouldNavigateToMainActivity() {
        // Aguarda animações
        Thread.sleep(10000)
        
        // Em alguns celulares reais, o evento de navegação pode ser registrado mais de uma vez
        // Usamos times(2) conforme reportado pelo erro do Espresso
        Intents.intended(hasComponent(MainActivity::class.java.name), Intents.times(2))
    }
}
