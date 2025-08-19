package org.biblioteca.mypersonallibrary.ui.screens

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.biblioteca.mypersonallibrary.data.Llibre
import org.biblioteca.mypersonallibrary.viewModel.LlibreViewModel

class FakeLlibreViewModel(
    llibresInicials: List<Llibre>
) : LlibreViewModel() {

    private val _llibres = MutableStateFlow(llibresInicials)
    override val totsElsLlibres: StateFlow<List<Llibre>> get() = _llibres

    // Sobreescriure mètodes que no fan falta en test
    override fun carregaTots() {}
    override fun eliminarLlibre(l: Llibre): Job = Job()
    override fun obrirLlibre(llibre: Llibre) {}
}
