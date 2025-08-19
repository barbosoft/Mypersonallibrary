package org.biblioteca.mypersonallibrary

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import org.biblioteca.mypersonallibrary.navigation.Screen
import org.biblioteca.mypersonallibrary.ui.screens.LlibreFormScreen
import org.biblioteca.mypersonallibrary.ui.screens.LlibreListScreen
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel


class MainActivity : ComponentActivity() {

    private val viewModel: LlibreViewModel by viewModels()

    // Demanar permís de CÀMERA en runtime
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchIsbnScanner()
        } else {
            Toast.makeText(this, "Cal el permís de càmera per escanejar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startScanWithPermission() {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) launchIsbnScanner()
        else requestCameraPermission.launch(Manifest.permission.CAMERA)
    }

    // Rebre el resultat de l’escaneig (Activity Result API)
    private val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val parsed = com.google.zxing.integration.android.IntentIntegrator
            .parseActivityResult(result.resultCode, result.data)
        if (parsed != null && parsed.contents != null && result.resultCode == Activity.RESULT_OK) {
            val isbn = parsed.contents
            Log.d("MainActivity", "ISBN escanejat: $isbn")
            viewModel.buscarPerIsbn(isbn)
        }
    }

    // Obrir directament el lector de ZXing (sense activitat pròpia)
    private fun launchIsbnScanner() {
        val integrator = IntentIntegrator(this).apply {
            // ISBN sol ser EAN-13 (1D)
            setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
            setPrompt("Escaneja l'ISBN")
            setBeepEnabled(true)
            setOrientationLocked(true)
            setCaptureActivity(CaptureActivity::class.java)
        }
        val intent: Intent = integrator.createScanIntent()
        scanLauncher.launch(intent)
    }

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val navController = rememberNavController()

            MaterialTheme {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Llistat.route
                ) {
                    composable(Screen.Llistat.route) {
                        LlibreListScreen (
                            viewModel= viewModel,
                            onEdit = { navController.navigate(Screen.Formulari.route) },
                            onNouLlibre = {
                                viewModel.netejarForm()
                                navController.navigate(Screen.Formulari.route)
                            }
                        )

                    }

                    composable(Screen.Formulari.route) {
                        LlibreFormScreen(
                            viewModel = viewModel,
                            onSave = { navController.popBackStack() },
                            onScanIsbn = { startScanWithPermission() }
                        )
                    }


                }

                /*
                LlibreFormScreen(
                    viewModel = viewModel,
                    onSave = {
                        Toast.makeText(this, "Llibre desat!", Toast.LENGTH_SHORT).show()
                    },
                    onScanIsbn = { startScanWithPermission() } // IMPORTANT: demana permís abans
                )

                 */
            }
        }
    }
}
