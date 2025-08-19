package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.ui.components.LlibreItem

@Composable
fun BooksList (

    llibres: List<Llibre>,
    onEdit: (Llibre) -> Unit,
    onEliminar: (Llibre) -> Unit

) {
    LazyColumn(modifier = Modifier.testTag("booksList")) {

        items(

            items = llibres,
            key = { it.id ?: it.hashCode().toLong() }

        ) {

            llibre ->
            LlibreItem(

                llibre = llibre,
                onEdit = { onEdit(llibre) },
                onEliminar = { onEliminar(llibre) }

            )

            Spacer(Modifier.height(8.dp))

        }
    }
}

@Composable
fun EmptyState (query: String, modifier: Modifier = Modifier) {

    Text(

        text = if (query.isBlank()) "No hi ha llibres." else "Cap resultat per \"$query\"",
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier

    )

}