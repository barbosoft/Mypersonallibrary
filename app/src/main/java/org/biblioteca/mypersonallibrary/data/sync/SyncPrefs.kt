package org.biblioteca.mypersonallibrary.data.sync

import android.content.Context

/**
 * Prefs molt simples per guardar l’últim sync de la Wishlist.
 * (Si prefereixes DataStore, ho podem canviar més endavant.)
 */
class SyncPrefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("wishlist_sync_prefs", Context.MODE_PRIVATE)

    fun setLastSync(ts: Long) {
        sp.edit().putLong(KEY_LAST_SYNC, ts).apply()
    }

    fun getLastSync(): Long = sp.getLong(KEY_LAST_SYNC, 0L)

    private companion object {
        const val KEY_LAST_SYNC = "last_sync"
    }
}

