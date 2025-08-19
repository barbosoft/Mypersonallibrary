package org.biblioteca.mypersonallibrary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.navigation.Screen
import org.biblioteca.mypersonallibrary.scanner.ScanActivity
import org.biblioteca.mypersonallibrary.ui.screens.LlibreFormScreen
import org.biblioteca.mypersonallibrary.ui.screens.LlibreListScreen
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel

class MainActivity : ComponentActivity() {

    private lateinit var vm: LlibreViewModel
    private var navController: NavHostController? = null

    private val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data?.getStringExtra(ScanActivity.EXTRA_SCAN_RESULT)
            if (!text.isNullOrBlank()) {
                vm.prepararNouLlibreAmbIsbn(text)
                vm.enriquirLlibrePerIsbn() // opcional
                // ✅ Passa la ROUTE (String), no l’objecte Screen
                navController?.navigate(Screen.LlibreForm.route)
            }
        }
    }

    private fun obrirEscaner() {
        scanLauncher.launch(Intent(this, ScanActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            vm = viewModel()
            val nav = rememberNavController()
            navController = nav

            MaterialTheme {
                AppNavHost(nav, vm, ::obrirEscaner)
            }
        }
    }
}

@Composable
private fun AppNavHost(
    nav: NavHostController,
    vm: LlibreViewModel,
    obrirEscaner: () -> Unit
) {
    NavHost(navController = nav, startDestination = Screen.LlibreList.route) {

        composable(Screen.LlibreList.route) {
            LlibreListScreen(
                viewModel = vm,
                onEdit = { nav.navigate(Screen.LlibreForm.route) },
                onNouLlibre = {
                    vm.obrirLlibre(Llibre()) // obre formulari en blanc
                    nav.navigate(Screen.LlibreForm.route)
                }
            )
        }

        composable(Screen.LlibreForm.route) {
            LlibreFormScreen(
                viewModel = vm,
                onSave = { nav.popBackStack() },
                onScanIsbn = { obrirEscaner() }
            )
        }
    }
}
