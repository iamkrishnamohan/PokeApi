package com.krrish.pokeapi.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.krrish.pokeapi.data.repositories.DataStoreRepository
import com.krrish.pokeapi.data.repositories.PokemonRepository
import com.krrish.pokeapi.model.PokemonResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    dataStoreRepository: DataStoreRepository
) :
    ViewModel() {

    val isDialogShown = dataStoreRepository.isDialogShownFlow

    private var currentResult: Flow<PagingData<PokemonResult>>? = null
    fun getPokemons(searchString: String?): Flow<PagingData<PokemonResult>> {
        val newResult: Flow<PagingData<PokemonResult>> =
            pokemonRepository.getPokemon(searchString).cachedIn(viewModelScope)
        currentResult = newResult
        return newResult
    }
}
