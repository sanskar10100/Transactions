package dev.sanskar.transactions.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dev.sanskar.transactions.R
import dev.sanskar.transactions.databinding.FragmentHomeBinding
import dev.sanskar.transactions.ui.model.MainViewModel

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val model by activityViewModels<MainViewModel>()
    private val adapter by lazy {
        TransactionsListAdapter(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.listTransactions.adapter = adapter

        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addTransactionFragment)
        }

        model.transactions.observe(viewLifecycleOwner) {
            var cashBalance = 0
            var digitalBalance = 0
            it.forEach { transaction ->
                if (transaction.isDigital) {
                    if (transaction.isExpense) {
                        digitalBalance -= transaction.amount
                    } else {
                        digitalBalance += transaction.amount
                    }
                } else {
                    if (transaction.isExpense) {
                        cashBalance -= transaction.amount
                    } else {
                        cashBalance += transaction.amount
                    }
                }
            }

            adapter.submitList(it)
            binding.textViewCashBalance.text = "₹$cashBalance"
            binding.textViewDigitalBalance.text = "₹$digitalBalance"
        }
    }
}