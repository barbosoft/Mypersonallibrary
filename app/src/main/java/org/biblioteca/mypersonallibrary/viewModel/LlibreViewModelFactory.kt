package org.biblioteca.mypersonallibrary.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.biblioteca.mypersonallibrary.data.LlibreRepository
import org.biblioteca.mypersonallibrary.data.RetrofitInstance
import org.biblioteca.mypersonallibrary.data.local.AppDatabase

class LlibreViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LlibreViewModel::class.java)) {
            val db  = AppDatabase.get(appContext)
            val dao = db.llibreDao()
            val api = RetrofitInstance.api
            val repo = LlibreRepository(dao, api)
            return LlibreViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}

