package org.biblioteca.mypersonallibrary.data.sync

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.coroutineScope
import org.biblioteca.mypersonallibrary.data.BibliotecaApi
import org.biblioteca.mypersonallibrary.data.RetrofitInstance
import org.biblioteca.mypersonallibrary.data.WishlistRepositoryHybrid
import org.biblioteca.mypersonallibrary.data.local.AppDatabase

class WishlistSyncWorker(
    appCtx: Context,
    params: WorkerParameters
) : CoroutineWorker(appCtx, params) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            // 1) DAOs
            val db = AppDatabase.get(applicationContext)
            val wishlistDao = db.wishlistDao()
            val llibreDao = db.llibreDao()           // <-- FALTAVA

            // 2) API compartida
            val api: BibliotecaApi = RetrofitInstance.api

            // 3) Repository híbrid
            val repo = WishlistRepositoryHybrid(
                dao = wishlistDao,
                api = api,
                prefs = null,
                llibreDao = llibreDao
            )

            // 4) Sync
            repo.sync()

            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }

    companion object {
        /** One-shot (per ex. després d’afegir/eliminar un element) */
        fun oneOff(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<WishlistSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED) // envia només amb xarxa
                        .build()
                )
                .build()

        /** Periòdic (ex.: cada 6h) */
        fun periodic(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<WishlistSyncWorker>(6, java.util.concurrent.TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
    }
}
