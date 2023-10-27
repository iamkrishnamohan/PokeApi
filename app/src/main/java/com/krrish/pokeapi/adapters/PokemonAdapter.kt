package com.krrish.pokeapi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.krrish.pokeapi.databinding.ListItemPokemonBinding
import com.krrish.pokeapi.model.PokemonResult
import com.krrish.pokeapi.utils.NETWORK_VIEW_TYPE
import com.krrish.pokeapi.utils.PRODUCT_VIEW_TYPE
import com.krrish.pokeapi.utils.getPicUrl
import java.util.Locale

class PokemonAdapter(private val navigate: (PokemonResult, Int, String?) -> Unit) :
    PagingDataAdapter<PokemonResult, PokemonAdapter.ViewHolder>(
        PokemonDiffCallback()
    ) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)!!
        holder.bind(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemPokemonBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    inner class ViewHolder(
        private val binding: ListItemPokemonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private var dominantColor: Int = 0
        private var picture: String? = ""
        fun bind(pokemonResult: PokemonResult) {
            binding.apply {
                pokemonItemTitle.text = pokemonResult.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.ENGLISH
                    ) else it.toString()
                }
                RecyclerViewBinding.loadImage(this, pokemonResult)
                picture = pokemonResult.url.getPicUrl()
                root.setOnClickListener {
                    navigate.invoke(pokemonResult, dominantColor, picture)
                }
            }

        }
    }

    private class PokemonDiffCallback : DiffUtil.ItemCallback<PokemonResult>() {
        override fun areItemsTheSame(oldItem: PokemonResult, newItem: PokemonResult): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: PokemonResult, newItem: PokemonResult): Boolean {
            return oldItem == newItem
        }
    }

    //checking if the pokemon are being displayed or the loading more progressbar inorder to set spans accordingly.
    override fun getItemViewType(position: Int): Int {
        return when (position) {
            itemCount -> NETWORK_VIEW_TYPE
            else -> PRODUCT_VIEW_TYPE
        }
    }
}

