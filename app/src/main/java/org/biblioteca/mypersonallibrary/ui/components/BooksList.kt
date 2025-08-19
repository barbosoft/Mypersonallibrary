package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.data.Llibre

@Composable
fun BooksList(
    llibres: List<Llibre>,
    onEdit: (Llibre) -> Unit,
    onEliminar: (Llibre) -> Unit,
    listState: LazyListState = rememberLazyListState() // 👈 nou
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.testTag("booksList")
    ) {
        items(
            items = llibres,
            key = { it.id ?: it.hashCode().toLong() }
        ) { l ->
            LlibreItem(
                llibre = l,
                onEdit = { onEdit(l) },
                onEliminar = { onEliminar(l) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
