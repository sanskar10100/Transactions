package dev.sanskar.transactions.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dev.sanskar.transactions.databinding.FragmentAddTransactionBinding
import dev.sanskar.transactions.ui.model.MainViewModel

class AddTransactionFragment : Fragment() {
    private val model by activityViewModels<MainViewModel>()
    private lateinit var binding: FragmentAddTransactionBinding
    val args: AddTransactionFragmentArgs by navArgs()
    var editMode = false

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

        editMode = args.transactionIndex >= 0

        if (editMode) {
            val transaction = model.transactions.value?.get(args.transactionIndex)
            if (transaction != null) {
                binding.textFieldAmount.editText?.setText(transaction.amount.toString())
                binding.textFieldDescription.editText?.setText(transaction.description)

                binding.chipExpense.isSelected = transaction.isExpense
//                binding.chipIncome.isSelected = !transaction.isExpense

                binding.chipDigital.isSelected = transaction.isDigital
//                binding.chipCash.isSelected = !transaction.isDigital
            }

            binding.buttonAdd.text = "Update"
        }

        binding.buttonAdd.setOnClickListener {
            // TODO: 04/12/21 Add validation
            val isDigital = !binding.chipCash.isChecked
            val isExpense = !binding.chipIncome.isChecked
            val amount = binding.textFieldAmount.editText?.text.toString().toInt()
            val description = binding.textFieldDescription.editText?.text.toString()

            if (editMode) {
                val transaction = model.transactions.value?.get(args.transactionIndex)
                if (transaction != null) {
                    model.updateTransaction(transaction.id, amount, description, isDigital, isExpense, transaction.timestamp)
                }
            } else {
                model.addTransaction(amount, description, isDigital, isExpense)
            }
            findNavController().popBackStack()
        }
    }
}