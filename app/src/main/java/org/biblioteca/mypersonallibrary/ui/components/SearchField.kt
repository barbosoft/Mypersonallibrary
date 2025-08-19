package org.biblioteca.mypersonallibrary.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
fun SearchField (

    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier

) {

    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    OutlinedTextField(

        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        label = { Text("Cerca") },
        placeholder = { Text("títol, autor, ISBN") },
        trailingIcon = {

            if (query.isNotBlank()) {

                IconButton(
                    onClick = {
                        onQueryChange("")
                        focusManager.clearFocus()
                        keyboard?.hide()
                    }
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Neteja cerca")
                }

            }

        },
        modifier = modifier
    )

}