package org.biblioteca.mypersonallibrary.domain

import org.biblioteca.mypersonallibrary.data.Llibre

enum class Ordre(val label: String) {

    RECENT("Recents"),
    TITOL("Títol"),
    AUTOR("Autor"),
    ISBN("ISBN")

}

fun filtreLlibres(llibres: List<Llibre>, query: String): List<Llibre> {

    if (query.isBlank()) return llibres
    val q = query.trim().lowercase()
    return llibres.filter { l ->

        (l.titol ?: "").lowercase().contains(q) ||
        (l.autor ?: "").lowercase().contains(q) ||
        (l.isbn ?: "").lowercase().contains(q)

    }
}

fun ordenaLlibres(llibres: List<Llibre>, ordre: Ordre): List<Llibre> =
    when (ordre) {

        Ordre.TITOL -> llibres.sortedBy { it.titol?.lowercase() ?: "" }
        Ordre.AUTOR -> llibres.sortedBy { it.autor?.lowercase() ?: "" }
        Ordre.ISBN -> llibres.sortedBy { it.isbn ?: "" }
        Ordre.RECENT -> TODO()
    }