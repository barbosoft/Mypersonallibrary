package org.biblioteca.mypersonallibrary.data.sync

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.coroutineScope
import org.biblioteca.mypersonallibrary.data.RetrofitInstance
import org.biblioteca.mypersonallibrary.data.WishlistRepositoryHybrid
import org.biblioteca.mypersonallibrary.data.local.AppDatabase

class WishlistSyncWorker(
    appCtx: Context,
    params: WorkerParameters
) : CoroutineWorker(appCtx, params) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            // 1) Obté la BD i el DAO
            val db  = AppDatabase.get(applicationContext)
            val dao = db.wishlistDao()

            // 2) Crea l'API de wishlist (pots reutilitzar la teva BASE_URL)
            val api = RetrofitInstance.wishlistApi(BASE_URL)

            // 3) Repository híbrid amb DAO (+ prefs si el teu constructor els demana)
            //    Si el constructor només demana (dao, api):
            val repo = WishlistRepositoryHybrid(dao, api)

            //    Si el teu constructor també demana SyncPrefs:
            // val prefs = SyncPrefs(applicationContext)
            // val repo = WishlistRepositoryHybrid(dao, api, prefs)

            // 4) Sincronitza
            repo.sync()

            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }

    companion object {
        const val BASE_URL = "http://192.168.1.145:8080/" // la teva URL

        fun oneOff(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<WishlistSyncWorker>().build()

        fun periodic(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<WishlistSyncWorker>(6, java.util.concurrent.TimeUnit.HOURS)
                .build()
    }
}
