package com.krrish.pokeapi.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.krrish.pokeapi.R
import com.krrish.pokeapi.adapters.LoadingStateAdapter
import com.krrish.pokeapi.adapters.PokemonAdapter
import com.krrish.pokeapi.databinding.FragmentPokemonListBinding
import com.krrish.pokeapi.model.PokemonResult
import com.krrish.pokeapi.utils.DOMINANT_COLOR
import com.krrish.pokeapi.utils.PICTURE
import com.krrish.pokeapi.utils.POKEMON_RESULT
import com.krrish.pokeapi.utils.PRODUCT_VIEW_TYPE
import com.krrish.pokeapi.utils.SCROLL_DURATION
import com.krrish.pokeapi.utils.hideSoftKeyboard
import com.krrish.pokeapi.utils.toast
import com.krrish.pokeapi.utils.toggle
import com.krrish.pokeapi.viewmodels.PokemonListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PokemonListFragment : Fragment() {
    private var hasInitiatedInitialCall = false
    private lateinit var binding: FragmentPokemonListBinding
    private val viewModel: PokemonListViewModel by viewModels()
    private var job: Job? = null
    private var hasUserSearched = false

    @Inject
    lateinit var thankYouDialog: ThankYouDialog

    private val adapter =
        PokemonAdapter { pokemonResult: PokemonResult, dominantColor: Int?, picture: String? ->
            navigate(
                pokemonResult,
                dominantColor,
                picture
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPokemonListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()
        checkDialog()
        setRefresh()
        setSearchView()

        binding.scrollUp.setOnClickListener {
            lifecycleScope.launch {
                binding.pokemonList.scrollToPosition(0)
                delay(SCROLL_DURATION)
                binding.scrollUp.toggle(false)
            }
        }
    }

    private fun setRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            startFetchingPokemon(null, true)

            binding.searchView.apply {
                text = null
                isFocusable = false

            }
            hideSoftKeyboard(requireActivity())

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSearchView() {
        binding.searchView.setOnTouchListener { v, _ ->
            v.isFocusableInTouchMode = true
            false
        }
        binding.searchView.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hasUserSearched = true
                binding.scrollUp.toggle(false)
                performSearch(binding.searchView.text.toString().trim())
                return@OnEditorActionListener true
            }
            false
        })
        binding.searchView.addTextChangedListener {

            if (it.toString().isEmpty() && hasUserSearched) {
                startFetchingPokemon(null, true)
                hideSoftKeyboard(requireActivity())
                hasUserSearched = false
            }
        }
    }

    private fun checkDialog() {
        lifecycleScope.launch {
            viewModel.isDialogShown.collect {
                if (it == null || it == false) {
                    thankYouDialog.show(childFragmentManager, null)
                }
            }
        }
    }

    private fun startFetchingPokemon(searchString: String?, shouldSubmitEmpty: Boolean) {
        //collecting flow then setting to adapter
        job?.cancel()
        job = lifecycleScope.launch {
            if (shouldSubmitEmpty) adapter.submitData(PagingData.empty())
            viewModel.getPokemons(searchString).collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun performSearch(searchString: String) {
        hideSoftKeyboard(requireActivity())
        if (searchString.isEmpty()) {
            requireContext().toast("Search cannot be empty")
            return
        }
        startFetchingPokemon(searchString, true)
    }

    private fun setAdapter() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = adapter.getItemViewType(position)
                return if (viewType == PRODUCT_VIEW_TYPE) 1
                else 2
            }
        }
        binding.pokemonList.layoutManager = gridLayoutManager
        binding.pokemonList.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter { adapter.retry() }
        )
        binding.pokemonList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val scrolledPosition =
                    (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                if (scrolledPosition != null) {
                    if (scrolledPosition >= 1) {
                        binding.scrollUp.toggle(true)
                    } else {
                        binding.scrollUp.toggle(false)
                    }
                }
            }
        })

        if (!hasInitiatedInitialCall) {
            startFetchingPokemon(null, false)
            hasInitiatedInitialCall = true
        }

        //the progress will only show when the adapter is refreshing and its empty
        adapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading && adapter.snapshot().isEmpty()
            ) {
                binding.progressCircular.isVisible = true
                binding.textError.isVisible = false
            } else {
                binding.progressCircular.isVisible = false
                binding.swipeRefreshLayout.isRefreshing = false

                //if there is error a textview will show the error encountered.
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                if (adapter.snapshot().isEmpty()) {
                    error?.let {
                        binding.textError.visibility = View.VISIBLE
                        binding.textError.setOnClickListener {
                            adapter.retry()
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.searchView.isFocusable = false
    }

    //navigating to stats fragment passing the pokemon and the dominant color
    private fun navigate(pokemonResult: PokemonResult, dominantColor: Int?, picture: String?) {
        val bundle = bundleOf(
            POKEMON_RESULT to pokemonResult,
            DOMINANT_COLOR to dominantColor,
            PICTURE to picture
        )
        findNavController().navigate(R.id.to_pokemonStatsFragment, bundle)
    }
}

