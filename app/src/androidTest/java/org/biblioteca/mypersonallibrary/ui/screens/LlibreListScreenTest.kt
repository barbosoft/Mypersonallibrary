package org.biblioteca.mypersonallibrary.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.domain.Ordre
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LlibreListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val llibresExemple = listOf(
        Llibre(id = 1, titol = "Kotlin en acció", autor = "Autor 1", isbn = "111"),
        Llibre(id = 2, titol = "Android Avançat", autor = "Autor 2", isbn = "222"),
        Llibre(id = 3, titol = "Zebra Patterns", autor = "Autor 3", isbn = "333")
    )

    @Test
    fun cerca_filtraLlista() {
        val fakeViewModel = FakeLlibreViewModel(llibresExemple)

        composeTestRule.setContent {
            LlibreListScreen(
                viewModel = fakeViewModel,
                onEdit = {},
                onNouLlibre = {}
            )
        }

        // Escriure "Kotlin" al camp de cerca
        composeTestRule.onNodeWithText("Cerca").performTextInput("Kotlin")

        // Comprovar que només apareix el llibre "Kotlin en acció"
        composeTestRule.onAllNodesWithText("Kotlin en acció")
            .assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Android Avançat")
            .assertCountEquals(0)
    }

    @Test
    fun netejarCerca_restaurarLlista() {
        composeTestRule.setContent {
            LlibreListScreenTestable(
                llibres = llibresExemple,
                loading = false,
                missatge = null,
                onRefresh = {},
                onEdit = {},
                onEliminar = {},
                onNouLlibre = {}
            )
        }

        // Filtra
        composeTestRule.onNodeWithText("Cerca").performTextInput("Kotlin")
        // S’ha d’haver filtrat a 1 targeta
        composeTestRule.onAllNodesWithTag("bookCard").assertCountEquals(1)

        // Neteja
        composeTestRule.onNodeWithContentDescription("Neteja cerca").performClick()
        // Torna a la mida original (3 targetes)
        composeTestRule.onAllNodesWithTag("bookCard").assertCountEquals(llibresExemple.size)
    }

    @Test
    fun canviOrdre_reordenaLlista() {
        val fakeViewModel = FakeLlibreViewModel(llibresExemple)

        composeTestRule.setContent {
            LlibreListScreen(
                viewModel = fakeViewModel,
                onEdit = {},
                onNouLlibre = {}
            )
        }

        // Obrir desplegable "Ordre" i triar ISBN
        composeTestRule.onNodeWithText("Ordre").performClick()
        composeTestRule.onNodeWithText(Ordre.ISBN.label).performClick()

        // Aquí pots afegir asserts sobre la nova disposició si cal
    }
}
