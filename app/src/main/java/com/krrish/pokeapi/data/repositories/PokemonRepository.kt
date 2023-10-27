package com.krrish.pokeapi.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.krrish.pokeapi.api.PokeApi
import com.krrish.pokeapi.data.datasource.PokemonDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepository @Inject constructor(private val pokemonApi: PokeApi) : BaseRepository() {

    //Returning the fetched data as flow
    fun getPokemon(searchString: String?) = Pager(
        config = PagingConfig(enablePlaceholders = false, pageSize = 25),
        pagingSourceFactory = {
            PokemonDataSource(pokemonApi, searchString)
        }
    ).flow

    suspend fun getSinglePokemon(id: Int) = safeApiCall {
        pokemonApi.getSinglePokemon(id)
    }
}
