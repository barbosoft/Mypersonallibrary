package org.biblioteca.mypersonallibrary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.data.RetrofitInstance          // 👈 AFEGIT
import org.biblioteca.mypersonallibrary.data.WishlistRepositoryHybrid  // 👈 AFEGIT
import org.biblioteca.mypersonallibrary.data.local.AppDatabase         // 👈 AFEGIT
import org.biblioteca.mypersonallibrary.data.sync.WishlistSyncWorker
// import org.biblioteca.mypersonallibrary.data.sync.SyncPrefs        // (opcional)
import org.biblioteca.mypersonallibrary.navigation.Screen
import org.biblioteca.mypersonallibrary.scanner.ScanActivity
import org.biblioteca.mypersonallibrary.ui.components.BusyOverlay
import org.biblioteca.mypersonallibrary.ui.components.NavBusyBinder
import org.biblioteca.mypersonallibrary.ui.components.rememberSmartBusy
import org.biblioteca.mypersonallibrary.ui.screens.LlibreEditScreen
import org.biblioteca.mypersonallibrary.ui.screens.LlibreFormScreen
import org.biblioteca.mypersonallibrary.ui.screens.LlibreListScreen
import org.biblioteca.mypersonallibrary.ui.screens.WishlistScreen
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel
import org.biblioteca.mypersonallibrary.viewModel.WishlistViewModel
import org.biblioteca.mypersonallibrary.viewModel.WishlistViewModelFactory // 👈 AFEGIT

class MainActivity : ComponentActivity() {

    private lateinit var vm: LlibreViewModel
    private lateinit var wishlistVM: WishlistViewModel
    private var navController: NavHostController? = null

    private val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data?.getStringExtra(ScanActivity.EXTRA_SCAN_RESULT)
            if (!text.isNullOrBlank()) {
                vm.prepararNouLlibreAmbIsbn(text)
                vm.enriquirLlibrePerIsbn()
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
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // ------ ViewModels ------
            vm = viewModel()

            // ✅ Crea DB → API → Repo → Factory → WishlistViewModel
            val db = AppDatabase.get(applicationContext)
            val api = RetrofitInstance.wishlistApi()               // ← API Retrofit per a wishlist
            // val prefs = SyncPrefs(applicationContext)            // (opcional)

            val wishlistRepo = WishlistRepositoryHybrid(
                dao = db.wishlistDao(),
                api = api
                // , prefs = prefs
            )
            wishlistVM = viewModel(factory = WishlistViewModelFactory(wishlistRepo))

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

        // ------------------------------------------------------------------
        // 🔄 Registra el Worker de sincronització de la Wishlist
        //    - Periodic: s'executa cada X hores (definit a WishlistSyncWorker.periodic())
        //    - Unique + KEEP evita duplicats si l’Activity es recrea
        // ------------------------------------------------------------------
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "wishlist_sync_periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            WishlistSyncWorker.periodic()
        )

        // (Opcional) Llança una sincronització immediata a l’arrencada
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
    // Overlay global “intel·ligent”
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
                    onOpenWishList = {                      // 👈 botó del carro
                        nav.navigate(Screen.Wishlist.route)
                    }
                )
            }

            composable(Screen.LlibreForm.route) {
                LaunchedEffect(Unit) { vm.endNav() }
                LlibreFormScreen(
                    viewModel = vm,
                    wishlistVM = wishlistVM,               // 👈 FALTAVA aquest paràmetre
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

