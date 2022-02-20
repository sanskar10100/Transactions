package dev.sanskar.transactions.ui.add

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dev.sanskar.transactions.KEY_DELETE_REQUEST
import dev.sanskar.transactions.KEY_DELETE_TRANSACTION_ID
import dev.sanskar.transactions.R
import dev.sanskar.transactions.asFormattedDateTime
import dev.sanskar.transactions.data.Transaction
import dev.sanskar.transactions.databinding.FragmentAddTransactionBinding
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig

class AddTransactionFragment : Fragment() {
    private val model by viewModels<AddViewModel>()
    private lateinit var binding: FragmentAddTransactionBinding
    private val args: AddTransactionFragmentArgs by navArgs()
    private var editMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        editMode = args.transactionId >= 0
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
            setFragmentResult(KEY_DELETE_REQUEST, bundleOf(
                KEY_DELETE_TRANSACTION_ID to args.transactionId
            ))
            findNavController().popBackStack()
        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onboard()
        if (editMode) {
            // Set received values in case of edit mode
            model.updateTransaction.observe(viewLifecycleOwner) {
                if (it != null) fillEditData(it)
            }    
            model.getTransactionForUpdate(args.transactionId)
        } else {
            binding.chipExpense.isChecked = true
            binding.chipDigital.isChecked = true
        }

        // Sets timestamp in human readable format
        binding.textViewTime.text = model.timestamp.asFormattedDateTime()

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
                model.updateTransaction.value?.let {
                    model.updateTransaction(
                        it.id,
                        amount,
                        description,
                        isDigital,
                        isExpense,
                        model.timestamp
                    )
                }
            } else {
                model.addTransaction(amount, description, model.timestamp, isDigital, isExpense)
            }
            findNavController().popBackStack()
        }
    }

    private fun setupTimePicker() {
        binding.buttonSetTime.setOnClickListener {
            findNavController().navigate(AddTransactionFragmentDirections.actionAddTransactionFragmentToTimePickerFragment(model.hour, model.minute))
        }

        parentFragmentManager.setFragmentResultListener("timePicker", viewLifecycleOwner) { _, bundle ->
            model.hour = bundle.getInt("hour")
            model.minute = bundle.getInt("minute")
            binding.textViewTime.text = model.timestamp.asFormattedDateTime()
        }
    }

    private fun setupDatePicker() {
        binding.buttonSetDate.setOnClickListener {
            findNavController().navigate(AddTransactionFragmentDirections.actionAddTransactionFragmentToDatePickerFragment(model.year, model.month, model.day))
        }

        parentFragmentManager.setFragmentResultListener("datePicker", viewLifecycleOwner) { _, bundle ->
            model.year = bundle.getInt("year")
            model.month = bundle.getInt("month")
            model.day = bundle.getInt("day")
            binding.textViewTime.text = model.timestamp.asFormattedDateTime()
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

        model.timestamp = transaction.timestamp
    }

    private fun onboard() {
        MaterialShowcaseSequence(requireActivity(), "1111").apply {
            setConfig(ShowcaseConfig().also { it.delay = 500 })
            addSequenceItem(MaterialShowcaseView.Builder(requireActivity())
                .setTarget(binding.chipGroupSource)
                .setSkipText("Skip")
                .setDismissText("Got it")
                .setContentText("Select medium of transaction. Default is digital.")
                .withRectangleShape()
                .build())
            addSequenceItem(MaterialShowcaseView.Builder(requireActivity())
                .setTarget(binding.chipGroupCategory)
                .setDismissText("Got it")
                .setContentText("Select type of transaction. Default is expense.")
                .withRectangleShape()
                .build())
            addSequenceItem(binding.buttonSetTime, "Click here to set time", "Got it")
            addSequenceItem(binding.buttonSetDate, "Click here to set date", "Got it")
            addSequenceItem(binding.textViewTime, "Current time is automatically set as default", "Got it")
            addSequenceItem(binding.buttonAdd, "When done, click here to add the transaction", "Got it")
            start()
        }
    }
}