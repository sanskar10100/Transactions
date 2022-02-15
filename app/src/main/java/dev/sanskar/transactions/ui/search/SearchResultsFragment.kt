package dev.sanskar.transactions.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dev.sanskar.transactions.databinding.FragmentSearchResultsBinding
import dev.sanskar.transactions.hide
import dev.sanskar.transactions.show
import dev.sanskar.transactions.ui.home.TransactionsListAdapter
import dev.sanskar.transactions.ui.model.MainViewModel

class SearchResultsFragment : Fragment() {
    private lateinit var binding: FragmentSearchResultsBinding
    private val model by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TransactionsListAdapter(requireContext())
        binding.listSearchResults.adapter = adapter
        model.searchResults.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.textViewNoMatches.show()
                binding.lottieEmpty.show()
            } else {
                adapter.submitList(it)
                binding.lottieEmpty.hide()
                binding.textViewNoMatches.hide()
            }
        }
    }
}