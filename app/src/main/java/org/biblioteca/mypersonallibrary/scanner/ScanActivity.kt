package org.biblioteca.mypersonallibrary.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
//import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.biblioteca.mypersonallibrary.ui.components.ScannerOverlay
import java.util.concurrent.Executors
import androidx.lifecycle.compose.LocalLifecycleOwner

class ScanActivity : ComponentActivity() {

    companion object { const val EXTRA_SCAN_RESULT = "SCAN_RESULT" }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) { setResult(Activity.RESULT_CANCELED); finish() }
            else recreate()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ScannerScreen(
                    onCancel = { setResult(Activity.RESULT_CANCELED); finish() },
                    onBarcode = { value ->
                        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_SCAN_RESULT, value))
                        finish()
                    },
                    requestCameraPermission = {
                        when (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                            PackageManager.PERMISSION_GRANTED -> true
                            else -> { cameraPermissionLauncher.launch(Manifest.permission.CAMERA); false }
                        }
                    }
                )
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScannerScreen(
    onCancel: () -> Unit,
    onBarcode: (String) -> Unit,
    requestCameraPermission: () -> Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { hasPermission = requestCameraPermission() }
    if (!hasPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sol·licitant permís de càmera…")
        }
        return
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    val options = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128, Barcode.FORMAT_QR_CODE
            ).build()
    }
    val scanner = remember { BarcodeScanning.getClient(options) }

    DisposableEffect(Unit) {
        onDispose { scanner.close(); executor.shutdown() }
    }

    fun bindCamera() {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analysis.setAnalyzer(executor) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                scanner.process(image)
                    .addOnSuccessListener { codes ->
                        codes.firstOrNull()?.rawValue?.let { value ->
                            try { cameraProvider.unbindAll() } catch (_: Exception) {}
                            imageProxy.close()
                            onBarcode(value)
                            return@addOnSuccessListener
                        }
                    }
                    .addOnFailureListener { /* ignore */ }
                    .addOnCompleteListener { imageProxy.close() }
            } else imageProxy.close()
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview, analysis
            )
        } catch (_: Exception) {}
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            cameraProviderFuture.addListener(
                { bindCamera() },
                ContextCompat.getMainExecutor(context)
            )
        }
    }

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Escaneja codi") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            ScannerOverlay(modifier = Modifier.fillMaxSize())
            Button(
                onClick = onCancel,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            ) { Text("Cancel·lar") }
        }
    }
}
