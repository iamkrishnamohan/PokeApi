package com.krrish.pokeapi.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PokemonResult(
    val name: String,
    val url: String
) : Parcelable
