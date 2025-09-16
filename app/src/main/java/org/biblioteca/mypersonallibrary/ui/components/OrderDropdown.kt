package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.domain.Ordre

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDropdown(
    order: Ordre,
    onOrderChange: (Ordre) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.widthIn(min = 140.dp, max = 220.dp)
    ) {
        OutlinedTextField(
            value = order.label,
            onValueChange = { },
            readOnly = true,
            singleLine = true,
            label = { Text("Ordre") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .widthIn(min = 140.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Ordre.entries.forEach { o ->
                DropdownMenuItem(
                    text = { Text(o.label) },
                    onClick = {
                        onOrderChange(o)
                        expanded = false
                    }
                )
            }
        }
    }
}
