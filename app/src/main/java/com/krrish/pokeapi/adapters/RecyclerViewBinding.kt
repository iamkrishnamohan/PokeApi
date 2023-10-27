package com.krrish.pokeapi.adapters

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.krrish.pokeapi.R
import com.krrish.pokeapi.databinding.ListItemPokemonBinding
import com.krrish.pokeapi.model.PokemonResult
import com.krrish.pokeapi.utils.getPicUrl

object RecyclerViewBinding {

    fun loadImage(
        binding: ListItemPokemonBinding, pokemonResult: PokemonResult
    ) {
        val picture = pokemonResult.url.getPicUrl()
        binding.apply {
            Glide.with(root)
                .load(picture)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressCircular.isVisible = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        val drawable = resource as BitmapDrawable
                        val bitmap = drawable.bitmap
                        Palette.Builder(bitmap).generate {
                            it?.let { palette ->
                                val dominantColor = palette.getDominantColor(
                                    ContextCompat.getColor(
                                        root.context,
                                        R.color.white
                                    )
                                )
                                pokemonItemImage.setBackgroundColor(dominantColor)
                            }
                        }
                        progressCircular.isVisible = false
                        return false
                    }

                })
                .into(pokemonItemImage)
        }
    }
}
