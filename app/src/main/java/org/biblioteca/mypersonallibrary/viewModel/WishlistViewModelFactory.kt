package org.biblioteca.mypersonallibrary.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.biblioteca.mypersonallibrary.data.WishlistRepositoryHybrid

class WishlistViewModelFactory(
    private val repo: WishlistRepositoryHybrid
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(WishlistViewModel::class.java))
        return WishlistViewModel(repo) as T
    }
}
