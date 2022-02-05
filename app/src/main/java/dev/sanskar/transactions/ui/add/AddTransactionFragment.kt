package dev.sanskar.transactions.ui.add

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dev.sanskar.transactions.R
import dev.sanskar.transactions.asFormattedDateTime
import dev.sanskar.transactions.data.Transaction
import dev.sanskar.transactions.databinding.FragmentAddTransactionBinding
import dev.sanskar.transactions.ui.model.MainViewModel

class AddTransactionFragment : Fragment() {
    private val model by activityViewModels<MainViewModel>()
    private val localModel by viewModels<AddTransactionFragmentModel>()
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
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (editMode) {
            // Set received values in case of edit mode
            val transaction = model.transactions.value?.get(args.transactionIndex)
            if (transaction != null) {
                fillEditData(transaction)
            }
        } else {
            binding.chipExpense.isChecked = true
            binding.chipDigital.isChecked = true
        }

        // Sets timestamp in human readable format
        binding.textViewTime.text = localModel.timestamp.asFormattedDateTime()

        setupTimePicker()
        setupDatePicker()

        binding.buttonAdd.setOnClickListener {
            val isDigital = !binding.chipCash.isChecked
            val isExpense = !binding.chipIncome.isChecked
            var amount: Int
            var description = ""
            binding.textFieldAmount.editText?.text.toString().run {
                if (this.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "Looks like you forgot to input amount!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                } else {
                    amount = try {
                        this.toInt()
                    } catch (e: NumberFormatException) {
                        // A floating point value was input
                        this.toFloat().toInt()
                    }
                }
            }
            binding.textFieldDescription.editText?.text.toString().run {
                if (this.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "Anonymous transaction added!",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                } else {
                    description = this
                }
            }

            if (editMode) {
                val transaction = model.transactions.value?.get(args.transactionIndex)
                if (transaction != null) {
                    model.updateTransaction(
                        transaction.id,
                        amount,
                        description,
                        isDigital,
                        isExpense,
                        localModel.timestamp
                    )
                }
            } else {
                model.addTransaction(amount, description, localModel.timestamp, isDigital, isExpense)
            }
            findNavController().popBackStack()
        }
    }

    private fun setupTimePicker() {
        binding.buttonSetTime.setOnClickListener {
            findNavController().navigate(AddTransactionFragmentDirections.actionAddTransactionFragmentToTimePickerFragment(localModel.hour, localModel.minute))
        }

        parentFragmentManager.setFragmentResultListener("timePicker", viewLifecycleOwner) { _, bundle ->
            localModel.hour = bundle.getInt("hour")
            localModel.minute = bundle.getInt("minute")
            binding.textViewTime.text = localModel.timestamp.asFormattedDateTime()
        }
    }

    private fun setupDatePicker() {
        binding.buttonSetDate.setOnClickListener {
            findNavController().navigate(AddTransactionFragmentDirections.actionAddTransactionFragmentToDatePickerFragment(localModel.year, localModel.month, localModel.day))
        }

        parentFragmentManager.setFragmentResultListener("datePicker", viewLifecycleOwner) { _, bundle ->
            localModel.year = bundle.getInt("year")
            localModel.month = bundle.getInt("month")
            localModel.day = bundle.getInt("day")
            binding.textViewTime.text = localModel.timestamp.asFormattedDateTime()
        }
    }

    private fun fillEditData(transaction: Transaction) {
        binding.textFieldAmount.editText?.setText(transaction.amount.toString())
        binding.textFieldDescription.editText?.setText(transaction.description)

        binding.chipExpense.isChecked = transaction.isExpense
        binding.chipIncome.isChecked = !transaction.isExpense

        binding.chipDigital.isChecked = transaction.isDigital
        binding.chipCash.isChecked = !transaction.isDigital
        binding.buttonAdd.text = "Update"

        localModel.timestamp = transaction.timestamp
    }
}