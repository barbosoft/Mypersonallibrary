package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.domain.Ordre

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDropdown(
    ordre: Ordre,
    onOrdreChange: (Ordre) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
            .widthIn(min = 140.dp, max = 220.dp)
            .semantics { testTag = "orderDropdown" }
    ) {
        OutlinedTextField(
            value = ordre.label,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Ordre") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .widthIn(min = 140.dp)
        )

        // Fem servir DropdownMenu + exposedDropdownSize() per no dependre d'ExposedDropdownMenu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize() // ajusta l'ample a l'anchor
        ) {
            Ordre.values().forEach { o ->
                DropdownMenuItem(
                    text = { Text(o.label) },
                    onClick = {
                        onOrdreChange(o)
                        expanded = false
                    }
                )
            }
        }
    }
}
