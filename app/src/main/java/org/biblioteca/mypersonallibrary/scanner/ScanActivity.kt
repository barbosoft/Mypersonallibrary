package org.biblioteca.mypersonallibrary.scanner

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator

fun AppCompatActivity.launchIsbnScanner(onResult: (String) -> Unit) {

    val integrator =IntentIntegrator(this)

    integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
    integrator.setPrompt("Escaneja l'ISBN")
    integrator.setCameraId(0)
    integrator.setBeepEnabled(true)
    integrator.setBarcodeImageEnabled(true)

    val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intentResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)

        if (intentResult != null && result.resultCode == Activity.RESULT_OK) {
            onResult(intentResult.contents ?: "")
        }
    }

    scanLauncher.launch(integrator.createScanIntent())

}