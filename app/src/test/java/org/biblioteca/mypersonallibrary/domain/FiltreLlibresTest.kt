package org.biblioteca.mypersonallibrary.domain

import org.biblioteca.mypersonallibrary.data.Llibre
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FiltreLlibresTest {
    private val llibres = listOf(
        Llibre(id=1, titol="Kotlin in Action", autor="Dmitry Jemerov", isbn="9781617293290"),
        Llibre(id=2, titol="Effective Java", autor="Joshua Bloch", isbn="9780134685991"),
        Llibre(id=3, titol="Clean Code", autor="Robert C. Martin", isbn="9780132350884"),
        Llibre(id=4, titol="El Quixot", autor="Cervantes", isbn="9788491050292")
    )

    @Test fun query_buida_retorna_original() {
        assertEquals(llibres, filtreLlibres(llibres, ""))
    }

    @Test fun filtra_per_titol_autor_isbn_case_insensitive() {
        assertTrue(filtreLlibres(llibres, "kotlin").any { it.titol?.contains("Kotlin") == true })
        assertTrue(filtreLlibres(llibres, "MARTIN").any { it.autor?.contains("Martin") == true })
        assertTrue(filtreLlibres(llibres, "0132350884").any { it.isbn?.contains("0132350884") == true })
    }

    @Test fun ordena_per_titol() {
        val r = ordenaLlibres(llibres, Ordre.TITOL)
        assertEquals(llibres.sortedBy { it.titol?.lowercase() ?: "" }, r)
    }

    @Test fun ordena_per_autor() {
        val r = ordenaLlibres(llibres, Ordre.AUTOR)
        assertEquals(llibres.sortedBy { it.autor?.lowercase() ?: "" }, r)
    }

    @Test fun ordena_per_isbn() {
        val r = ordenaLlibres(llibres, Ordre.ISBN)
        assertEquals(llibres.sortedBy { it.isbn ?: "" }, r)
    }
}
