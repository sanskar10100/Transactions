package dev.sanskar.transactions.ui.bottomdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.sanskar.transactions.KEY_AMOUNT
import dev.sanskar.transactions.KEY_FILTER_BY_AMOUNT
import dev.sanskar.transactions.KEY_SELECTED_OPTION_INDEX
import dev.sanskar.transactions.databinding.FragmentAmountFilterBottomSheetBinding
import dev.sanskar.transactions.text

class AmountFilterBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAmountFilterBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAmountFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setInitial()

        binding.buttonFilter.setOnClickListener {
            if (binding.textFieldAmount.text.isEmpty()) {
                binding.textFieldAmount.error = "Empty Amount!"
                return@setOnClickListener
            }
            val amount = try {
                binding.textFieldAmount.text.toInt()
            } catch (e: NumberFormatException) {
                binding.textFieldAmount.text.toFloat().toInt()
            }
            val optionIndex = if (binding.chipGreaterThan.isChecked) 0 else 1
            returnResult(amount, optionIndex)
        }

        binding.buttonClearFilter.setOnClickListener {
            returnResult(-1, -1) // This will automatically clear the query config
        }
    }

    private fun setInitial() {
        val args by navArgs<AmountFilterBottomSheetArgs>()
        if (args.selectedIndex == 0) {
            binding.chipGreaterThan.isChecked = true
        } else if (args.selectedIndex == 1) {
            binding.chipLessThan.isChecked = true
        }
        binding.textFieldAmount.editText?.setText(if (args.amount != -1) args.amount.toString() else "")
    }

    private fun returnResult(amount: Int, optionIndex: Int) {
        setFragmentResult(KEY_FILTER_BY_AMOUNT, bundleOf(
            KEY_AMOUNT to amount,
            KEY_SELECTED_OPTION_INDEX to optionIndex
        ))
        dismiss()
    }
}