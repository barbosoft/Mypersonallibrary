package org.biblioteca.mypersonallibrary.ui.screens

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.domain.Ordre
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

        // Escriure al camp de cerca (testTag robust)
        composeTestRule.onNodeWithTag("searchField").performTextInput("Kotlin")

        // S'ha d'haver filtrat a 1 targeta i contenir el llibre esperat
        composeTestRule.onAllNodesWithTag("bookCard").assertCountEquals(1)
        composeTestRule.onNodeWithText("Kotlin en acció").assertExists()
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

        // Filtra → 1 card
        composeTestRule.onNodeWithTag("searchField").performTextInput("Kotlin")
        composeTestRule.onAllNodesWithTag("bookCard").assertCountEquals(1)

        // Neteja → recupera la mida original
        composeTestRule.onNodeWithContentDescription("Neteja cerca").performClick()
        composeTestRule.onAllNodesWithTag("bookCard").assertCountEquals(llibresExemple.size)
    }

    @Test
    fun canviOrdre_reordenaLlista() {
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

        // Obrir desplegable "Ordre" i seleccionar "Títol"
        composeTestRule.onNodeWithTag("orderDropdown").performClick()
        composeTestRule.onNodeWithText(Ordre.TITOL.label).performClick()

        // Smoke check: segueixen els 3 ítems (la interacció no ha fallat)
        composeTestRule.onAllNodesWithTag("bookCard").assertCountEquals(llibresExemple.size)

        // (Opcional) Per assertar l'ordre estrictament:
        // - Posa un testTag al Text del títol dins LlibreItem (p. ex. "bookTitle-<index>" o "bookTitle")
        // - Comprova que el primer card conté "Android Avançat" (alfabètic per títol)
    }
}
