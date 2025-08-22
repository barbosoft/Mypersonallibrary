package org.biblioteca.mypersonallibrary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.navigation.Screen
import org.biblioteca.mypersonallibrary.scanner.ScanActivity
import org.biblioteca.mypersonallibrary.ui.components.BusyOverlay
import org.biblioteca.mypersonallibrary.ui.components.NavBusyBinder
import org.biblioteca.mypersonallibrary.ui.components.rememberSmartBusy
import org.biblioteca.mypersonallibrary.ui.screens.LlibreEditScreen
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
                vm.enriquirLlibrePerIsbn()
                vm.startNav()                                // 👈 mostra overlay mentre navega
                navController?.navigate(Screen.LlibreForm.route)
            }
        }
    }

    private fun obrirEscaner() {
        scanLauncher.launch(Intent(this, ScanActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 👇 Permet que Compose gestioni sistemes i IME insets moderns
        WindowCompat.setDecorFitsSystemWindows(window, false)
        //enableEdgeToEdge()
        setContent {
            vm = viewModel()
            val nav = rememberNavController()
            navController = nav

            MaterialTheme {
                AppNavHost(nav = nav, vm = vm, obrirEscaner = ::obrirEscaner)
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
    // Overlay global “intel·ligent”
    val rawBusy by vm.busy.collectAsState()
    val busy by rememberSmartBusy(rawBusy, showDelayMs = 0, minShowMs = 600)

    Box(Modifier.fillMaxSize()) {
        NavBusyBinder(nav, vm)
        NavHost(navController = nav, startDestination = Screen.LlibreList.route) {

            composable(Screen.LlibreList.route) {
                // Atura overlay en entrar a la pantalla
                LaunchedEffect(Unit) { vm.endNav() }

                LlibreListScreen(
                    viewModel = vm,
                    onEdit = { l ->
                        vm.obrirLlibre(l)
                        vm.startNav()
                        nav.navigate(Screen.LlibreEdit.route)
                    },
                    onNouLlibre = {
                        vm.obrirLlibre(Llibre())
                        vm.startNav()
                        nav.navigate(Screen.LlibreForm.route)
                    }
                )
            }

            composable(Screen.LlibreForm.route) {
                LaunchedEffect(Unit) { vm.endNav() }

                LlibreFormScreen(
                    viewModel = vm,
                    onSave = {
                        vm.startNav()
                        nav.popBackStack()
                    },
                    onScanIsbn = { obrirEscaner() }
                )
            }

            composable(Screen.LlibreEdit.route) {
                LaunchedEffect(Unit) { vm.endNav() }

                LlibreEditScreen(
                    viewModel = vm,
                    onDone = {
                        vm.startNav()
                        nav.popBackStack()
                    },
                    onCancel = {
                        vm.startNav()
                        nav.popBackStack()
                    }
                )
            }
        }

        BusyOverlay(show = busy)
    }
}
