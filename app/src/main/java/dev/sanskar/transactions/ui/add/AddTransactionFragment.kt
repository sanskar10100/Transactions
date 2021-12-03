package dev.sanskar.transactions.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dev.sanskar.transactions.databinding.FragmentAddTransactionBinding
import dev.sanskar.transactions.ui.model.MainViewModel

class AddTransactionFragment : Fragment() {
    private val model by activityViewModels<MainViewModel>()
    private lateinit var binding: FragmentAddTransactionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAdd.setOnClickListener {
            // TODO: 04/12/21 Add validation
            val isDigital = !binding.chipCash.isChecked
            val isExpense = !binding.chipIncome.isChecked
            val amount = binding.textFieldAmount.editText?.text.toString().toInt()
            val description = binding.textFieldDescription.editText?.text.toString()

            model.addTransaction(amount, description, isDigital, isExpense)
            findNavController().popBackStack()
        }
    }
}