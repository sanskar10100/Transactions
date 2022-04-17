package dev.sanskar.transactions.ui.add

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.play.core.review.ReviewManagerFactory
import dev.sanskar.transactions.*
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
            model.setValuesIfEdit(args.transactionId).observe(viewLifecycleOwner) {
                if (true) {
                    setInitial() // Wait until values are loaded in ViewModel
                }
            }
        } else {
            setInitial()
        }

        binding.buttonAdd.setOnClickListener {
            binding.textFieldAmount.editText?.text.toString().run {
                if (this.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "Looks like you forgot to input amount!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                } else {
                    model.amount = try {
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
                    model.description = this
                }
            }

            model.isExpense = binding.chipExpense.isChecked
            model.isDigital = binding.chipDigital.isChecked

            if (editMode) model.updateTransaction(args.transactionId) else model.addTransaction()
            askForPlayStoreReview()
        }
    }

    private fun askForPlayStoreReview() {
        model.hasAddedTenTransactions().observe(viewLifecycleOwner) {
            log("Attempting to make review request")
            val manager = ReviewManagerFactory.create(requireContext())
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    log("Review request successfully attempted")
                    manager.launchReviewFlow(requireActivity(), task.result).addOnCompleteListener {
                        findNavController().popBackStack()
                    }
                } else {
                    log("Failed to launch review popup with exception: ${task.exception?.message}")
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setInitial() {
        with (binding) {
            textFieldAmount.text = if (model.amount > 0) model.amount.toString() else ""
            textFieldDescription.text = model.description.ifEmpty { "" }
            chipDigital.isChecked = model.isDigital
            chipExpense.isChecked = model.isExpense
            chipCash.isChecked = !model.isDigital
            chipIncome.isChecked = !model.isExpense
            buttonSetDate.text = model.getDate()
            buttonSetTime.text = model.getTime()
        }
        setupTimePicker()
        setupDatePicker()
    }

    private fun setupTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(model.hour)
            .setMinute(model.minute)
            .setTitleText("Select time")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            model.hour = timePicker.hour
            model.minute = timePicker.minute
            binding.buttonSetTime.text = model.getTime()
        }

        binding.buttonSetTime.setOnClickListener { timePicker.show(parentFragmentManager, "time-picker") }
    }

    private fun setupDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a date")
            .setSelection(model.constructTimestamp())
            .build()

        datePicker.addOnPositiveButtonClickListener {
            datePicker.selection?.let { model.setDateFromTimestamp(it) }
            binding.buttonSetDate.text = model.getDate()
        }

        binding.buttonSetDate.setOnClickListener {
            datePicker.show(parentFragmentManager, "date-picker")
        }
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
            addSequenceItem(binding.buttonAdd, "When done, click here to add the transaction", "Got it")
            start()
        }
    }
}