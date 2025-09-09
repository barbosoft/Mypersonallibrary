package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.ui.screens.PortadaLlibre
import androidx.compose.ui.draw.clip

/**
 * Llista de llibres:
 *  - Cada fila és clickable -> crida onEdit(l) (navegar a edició)
 *  - Sense botó “Editar”
 *  - Amb botó “Eliminar”
 *  - Si no hi ha imatge, mostra placeholder (via PortadaLlibre)
 */
@Composable
fun BooksList(
    llibres: List<Llibre>,
    onEdit: (Llibre) -> Unit,
    onEliminar: (Llibre) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = llibres,
            key = { it.id ?: it.isbn ?: (it.titol + it.autor).hashCode() }
        ) { l ->
            BookRow(
                llibre = l,
                onClick = { onEdit(l) },
                onEliminar = { onEliminar(l) }
            )
        }
    }
}

@Composable
private fun BookRow(
    llibre: Llibre,
    onClick: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                contentDescription = "llibreItem"
            }
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // ------- Portada amb fallback integrat -------
            PortadaLlibre(
                imageUrl = llibre.imatgeUrl,
                contentDescription = "Portada — ${llibre.titol ?: "Sense títol"}",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.width(12.dp))

            // ------- Textos -------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = llibre.titol?.ifBlank { "—" } ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buildString {
                        append(llibre.autor?.ifBlank { "—" } ?: "—")
                        llibre.isbn?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // ------- Accions (només Eliminar) -------
            IconButton(
                onClick = onEliminar,
                modifier = Modifier.semantics { contentDescription = "btnEliminar" }
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null)
            }
        }
    }
}
