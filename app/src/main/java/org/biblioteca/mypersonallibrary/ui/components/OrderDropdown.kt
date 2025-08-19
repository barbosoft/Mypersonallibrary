package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.biblioteca.mypersonallibrary.domain.Ordre

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDropdown (

    ordre: Ordre,
    onOrdreChange: (Ordre) -> Unit,
    modifier: Modifier = Modifier

) {

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(

        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.widthIn(min = 140.dp, max = 220.dp)

    ) {
        OutlinedTextField(

            value = ordre.label,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Ordre") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor()
                .widthIn(min = 140.dp)

        )
        ExposedDropdownMenu(

            expanded = expanded,
            onDismissRequest = { expanded = false }

        ) {

            Ordre.values().forEach { o ->
                DropdownMenuItem(

                    text = { Text(o.label) },
                    onClick = { onOrdreChange(o); expanded = false }

                )
            }

        }
    }

}