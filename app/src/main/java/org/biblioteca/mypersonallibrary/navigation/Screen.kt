package org.biblioteca.mypersonallibrary.navigation

sealed class Screen(val route: String) {

    object Llistat : Screen("llistat")
    object Formulari : Screen("formulari")

}