package dev.sanskar.transactions.ui.add

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dev.sanskar.transactions.R
import dev.sanskar.transactions.databinding.FragmentAddTransactionBinding
import dev.sanskar.transactions.ui.model.MainViewModel

class AddTransactionFragment : Fragment() {
    private val model by activityViewModels<MainViewModel>()
    private lateinit var binding: FragmentAddTransactionBinding
    private val args: AddTransactionFragmentArgs by navArgs()
    private var editMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        editMode = args.transactionIndex >= 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (editMode) inflater.inflate(R.menu.add_transaction_options_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete_transaction) {
            val transaction = model.transactions.value?.get(args.transactionIndex)
            if (transaction != null) {
                model.deleteTransaction(transaction)
                findNavController().popBackStack()
            }
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (editMode) {
            val transaction = model.transactions.value?.get(args.transactionIndex)
            if (transaction != null) {
                binding.textFieldAmount.editText?.setText(transaction.amount.toString())
                binding.textFieldDescription.editText?.setText(transaction.description)

                binding.chipExpense.isChecked = transaction.isExpense
                binding.chipIncome.isChecked = !transaction.isExpense

                binding.chipDigital.isChecked = transaction.isDigital
                binding.chipCash.isChecked = !transaction.isDigital
            }

            binding.buttonAdd.text = "Update"
        } else {
            binding.chipExpense.isChecked = true
            binding.chipDigital.isChecked = true
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