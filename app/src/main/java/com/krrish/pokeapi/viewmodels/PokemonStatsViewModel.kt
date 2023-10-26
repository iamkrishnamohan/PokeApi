package com.krrish.pokeapi.viewmodels

import androidx.lifecycle.ViewModel
import com.krrish.pokeapi.data.repositories.PokemonRepository
import com.krrish.pokeapi.utils.NetworkResource
import com.krrish.pokeapi.utils.extractId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class PokemonStatsViewModel @Inject constructor(private val pokemonRepository: PokemonRepository) :
    ViewModel() {

    suspend fun getSinglePokemon(url: String) = flow {
        val id = url.extractId()
        emit(NetworkResource.Loading)
        emit(pokemonRepository.getSinglePokemon(id))
    }
}

