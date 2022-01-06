package dev.sanskar.transactions.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dev.sanskar.transactions.databinding.FragmentFilterResultBinding
import dev.sanskar.transactions.ui.home.TransactionsListAdapter

class FilterResultFragment : Fragment() {
    private lateinit var binding: FragmentFilterResultBinding
    private val model by activityViewModels<FilterViewModel>()
    private val adapter by lazy {
        TransactionsListAdapter(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFilterResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listFilteredTransactions.adapter = adapter

        model.filteredTransactions.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}