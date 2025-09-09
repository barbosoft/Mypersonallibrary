package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.domain.Ordre

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListFiltersBar(
    query: String,
    onQueryChange: (String) -> Unit,
    order: Ordre,
    onOrderChange: (Ordre) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Reutilitza el teu SearchField existent
        org.biblioteca.mypersonallibrary.ui.components.SearchField(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier.weight(1f)
        )

        OrderDropdown(
            order = order,
            onOrderChange = onOrderChange
        )
    }
}
