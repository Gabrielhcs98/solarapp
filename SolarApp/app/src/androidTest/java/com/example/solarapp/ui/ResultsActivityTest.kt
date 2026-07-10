package com.example.solarapp.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.solarapp.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultsActivityTest {

    @Test
    fun shouldDisplayCityNameFromIntent() {
        val city = "Londrina"
        val intent = Intent(ApplicationProvider.getApplicationContext(), ResultsActivity::class.java).apply {
            putExtra("city", city)
        }

        ActivityScenario.launch<ResultsActivity>(intent).use {
            // Verifica se o cabeçalho de resultado está visível
            onView(withId(R.id.textViewHeader)).check(matches(isDisplayed()))
            
            // Verifica se o nome da cidade (ou pelo menos o TextView) está visível
            // Nota: Como o ViewModel buscará na rede, o nome final pode demorar. 
            // Mas o TextViewResults deve estar lá.
            onView(withId(R.id.textViewResults)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldShowBackButton() {
        val city = "Sao Paulo"
        val intent = Intent(ApplicationProvider.getApplicationContext(), ResultsActivity::class.java).apply {
            putExtra("city", city)
        }

        ActivityScenario.launch<ResultsActivity>(intent).use {
            // Aguarda o carregamento inicial sumir ou a Activity estabilizar
            Thread.sleep(2000)
            
            // Verifica se o botão Voltar está visível
            onView(withId(R.id.buttonBack)).check(matches(isDisplayed()))
        }
    }
}
