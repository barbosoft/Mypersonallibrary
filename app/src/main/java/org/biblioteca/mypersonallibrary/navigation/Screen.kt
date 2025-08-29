package org.biblioteca.mypersonallibrary.navigation

sealed class Screen(val route: String) {
    data object LlibreList : Screen("llibre_list")
    data object LlibreForm : Screen("llibre_form")
    data object LlibreEdit : Screen("llibre_edit")
    data object Wishlist : Screen("wishlist")
}
