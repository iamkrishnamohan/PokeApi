package com.krrish.pokeapi.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.krrish.pokeapi.adapters.StatsAdapter
import com.krrish.pokeapi.databinding.FragmentPokemonStatsBinding
import com.krrish.pokeapi.model.PokemonResult
import com.krrish.pokeapi.model.Stats
import com.krrish.pokeapi.utils.DOMINANT_COLOR
import com.krrish.pokeapi.utils.NetworkResource
import com.krrish.pokeapi.utils.PICTURE
import com.krrish.pokeapi.utils.POKEMON_RESULT
import com.krrish.pokeapi.utils.STATS_DIV_DURATION
import com.krrish.pokeapi.utils.STATS_DURATION
import com.krrish.pokeapi.utils.toast
import com.krrish.pokeapi.viewmodels.PokemonStatsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class PokemonStatsFragment : Fragment() {
    private lateinit var binding: FragmentPokemonStatsBinding
    private val adapter = StatsAdapter()

    private val viewModel: PokemonStatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPokemonStatsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundleTitle = this.arguments
        if (bundleTitle != null) {
            val pokemonResult =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundleTitle.getParcelable(POKEMON_RESULT, PokemonResult::class.java)
                } else {
                    bundleTitle.getParcelable(POKEMON_RESULT)
                }
            val dominantColor = bundleTitle.getInt(DOMINANT_COLOR)
            val picture = bundleTitle.getString(PICTURE)


            //setting the colors based on dominant colors
            dominantColor.let { theColor ->
                binding.card.setBackgroundColor(theColor)
                binding.toolbar.setBackgroundColor(theColor)
                requireActivity().window.statusBarColor = theColor
            }

            val toolbar = binding.toolbar
            toolbar.elevation = 0.0F
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
            (activity as AppCompatActivity).supportActionBar!!.title =
                pokemonResult?.name?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                    else it.toString()
                }
            (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(true)

            toolbar.setNavigationOnClickListener {
                binding.root.findNavController().navigateUp()
            }

            //load pic
            binding.apply {
                Glide.with(root)
                    .load(picture)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(pokemonItemImage)
            }

            pokemonResult?.let { loadSinglePokemon(it) }
        }
    }


    private fun loadSinglePokemon(pokemonResult: PokemonResult) {

        lifecycleScope.launch(Dispatchers.Main) {
            //a bit delay for the animation to finish
            delay(STATS_DURATION)
            viewModel.getSinglePokemon(pokemonResult.url).collect {
                when (it) {
                    is NetworkResource.Success -> {
                        binding.progressCircular.isVisible = false
                        binding.apply {
                            (it.value.weight.div(STATS_DIV_DURATION)
                                .toString() + " kgs").also { weight ->
                                pokemonItemWeight.text = weight
                            }
                            (it.value.height.div(STATS_DIV_DURATION)
                                .toString() + " metres").also { height ->
                                pokemonItemHeight.text = height
                            }
                            pokemonStatList.adapter = adapter
                            adapter.setStats(it.value.stats as ArrayList<Stats>)
                        }
                    }

                    is NetworkResource.Failure -> {
                        binding.progressCircular.isVisible = false
                        requireContext().toast("There was an error loading the pokemon")
                    }

                    is NetworkResource.Loading -> {
                        binding.progressCircular.isVisible = true
                    }
                }
            }
        }
    }
}
