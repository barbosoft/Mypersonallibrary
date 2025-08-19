package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.ui.screens.PortadaLlibre // Reutilitzem el que ja tens

@Composable
fun LlibreItem(
    llibre: Llibre,
    onEdit: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(Modifier
        .fillMaxWidth()
        .testTag("bookCard")
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 📘 Portada
                PortadaLlibre(
                    imageUrl = llibre.imatgeUrl,
                    contentDescription = llibre.titol,
                    modifier = Modifier.size(width = 72.dp, height = 96.dp) // proporció 3:4
                )

                // 📄 Dades bàsiques
                Column(Modifier.weight(1f)) {
                    Text(
                        llibre.titol ?: "(Sense títol)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (!llibre.autor.isNullOrBlank()) {
                        Text("Autor: ${llibre.autor}")
                    }
                    if (!llibre.isbn.isNullOrBlank()) {
                        Text("ISBN: ${llibre.isbn}")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Editar") }
                OutlinedButton(onClick = onEliminar, modifier = Modifier.weight(1f)) { Text("Eliminar") }
            }
        }
    }
}
