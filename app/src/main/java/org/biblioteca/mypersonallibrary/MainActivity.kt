package org.biblioteca.mypersonallibrary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.data.RetrofitInstance
import org.biblioteca.mypersonallibrary.data.WishlistRepositoryHybrid
import org.biblioteca.mypersonallibrary.data.local.AppDatabase
import org.biblioteca.mypersonallibrary.data.sync.WishlistSyncWorker
import org.biblioteca.mypersonallibrary.navigation.Screen
import org.biblioteca.mypersonallibrary.scanner.ScanActivity
import org.biblioteca.mypersonallibrary.ui.components.BusyOverlay
import org.biblioteca.mypersonallibrary.ui.components.NavBusyBinder
import org.biblioteca.mypersonallibrary.ui.components.rememberSmartBusy
import org.biblioteca.mypersonallibrary.ui.screens.LlibreEditScreen
import org.biblioteca.mypersonallibrary.ui.screens.LlibreFormScreen
import org.biblioteca.mypersonallibrary.ui.screens.LlibreListScreen
import org.biblioteca.mypersonallibrary.ui.screens.WishlistScreen
import org.biblioteca.mypersonallibrary.viewModel.*

class MainActivity : ComponentActivity() {

    // ⬇️ Un ÚNIC LlibreViewModel a nivell d’Activity (amb la seva Factory)
    private val llibreVM: LlibreViewModel by viewModels {
        LlibreViewModelFactory(applicationContext)
    }

    private var navController: NavHostController? = null

    private val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data?.getStringExtra(ScanActivity.EXTRA_SCAN_RESULT)
            if (!text.isNullOrBlank()) {
                // ⬇️ Fem servir el ViewModel d’Activity (sempre inicialitzat)
                llibreVM.prepararNouLlibreAmbIsbn(text)
                llibreVM.enriquirLlibrePerIsbn()
                navController?.navigate(Screen.LlibreForm.route) {
                    launchSingleTop = true
                }
            }
        }
    }

    private fun obrirEscaner() {
        scanLauncher.launch(Intent(this, ScanActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Fem servir el mateix LlibreVM d’Activity a la UI
            val vm = llibreVM

            // ✅ Crea (o obté) DB/API/Repo una vegada per composició
            //val db by remember { mutableStateOf(AppDatabase.get(applicationContext)) }
            val db = remember { AppDatabase.get(applicationContext) }
            val api = RetrofitInstance.api

            val wishlistRepo = remember { WishlistRepositoryHybrid(dao = db.wishlistDao(), api = api, llibreDao = db.llibreDao()) }

            // Wishlist VM només per a la UI (no cal guardar-lo com a camp d’Activity)
            val wishlistVM: WishlistViewModel =
                viewModel(factory = WishlistViewModelFactory(wishlistRepo))

            val nav = rememberNavController()
            navController = nav

            MaterialTheme {
                AppNavHost(
                    nav = nav,
                    vm = vm,
                    wishlistVM = wishlistVM,
                    obrirEscaner = ::obrirEscaner
                )
            }
        }

        // 🔄 Workers de sincronització (fora de Compose)
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "wishlist_sync_periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            WishlistSyncWorker.periodic()
        )
        WorkManager.getInstance(applicationContext)
            .enqueue(WishlistSyncWorker.oneOff())
    }
}

@Composable
private fun AppNavHost(
    nav: NavHostController,
    vm: LlibreViewModel,
    wishlistVM: WishlistViewModel,
    obrirEscaner: () -> Unit
) {
    val rawBusy by vm.busy.collectAsState()
    val busy by rememberSmartBusy(rawBusy, showDelayMs = 0, minShowMs = 600)

    Box(Modifier.fillMaxSize()) {

        NavBusyBinder(nav, vm)

        NavHost(navController = nav, startDestination = Screen.LlibreList.route) {

            composable(Screen.LlibreList.route) {
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
                    },
                    onOpenWishList = { nav.navigate(Screen.Wishlist.route) }
                )
            }

            composable(Screen.LlibreForm.route) {
                LaunchedEffect(Unit) { vm.endNav() }
                LlibreFormScreen(
                    viewModel = vm,
                    wishlistVM = wishlistVM,
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

            composable(Screen.Wishlist.route) {
                LaunchedEffect(Unit) { vm.endNav() }
                WishlistScreen(
                    llibresVM = vm,
                    wishlistVM = wishlistVM,
                    onBack = { nav.popBackStack() }
                )
            }
        }

        BusyOverlay(show = busy)
    }
}

        BusyOverlay(show = busy)
    }
}
