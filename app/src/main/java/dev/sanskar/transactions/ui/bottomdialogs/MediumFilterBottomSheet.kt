package dev.sanskar.transactions.ui.bottomdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dev.sanskar.transactions.KEY_FILTER_BY_MEDIUM
import dev.sanskar.transactions.KEY_MEDIUM
import dev.sanskar.transactions.collectWithLifecycle
import dev.sanskar.transactions.data.FilterByMediumChoices
import dev.sanskar.transactions.databinding.FragmentMediumFilterBottomSheetBinding
import dev.sanskar.transactions.shortSnackbar
import dev.sanskar.transactions.ui.home.HomeFragmentDirections

class MediumFilterBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentMediumFilterBottomSheetBinding
    private val viewModel by viewModels<MediumFilterBottomSheetViewModel>()
    private val args by navArgs<MediumFilterBottomSheetArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMediumFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initialize(args.selectedMediums)

        viewModel.medium.collectWithLifecycle {
            binding.checkboxCash.isChecked = it.cash
            binding.checkboxDigital.isChecked = it.digital
            binding.checkboxCredit.isChecked = it.credit
        }

        binding.checkboxCash.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleCash(isChecked)
        }

        binding.checkboxDigital.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleDigital(isChecked)
        }

        binding.checkboxCredit.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleCredit(isChecked)
        }

        binding.buttonReset.setOnClickListener {
            viewModel.reset()
            returnResult()
        }

        binding.buttonApply.setOnClickListener {
            if (viewModel.isAnyMediumSelected()) {
                returnResult()
                dismiss()
            } else {
                binding.root.shortSnackbar("Select at least one medium")
            }
        }
    }

    fun returnResult() {
        setFragmentResult(
            KEY_FILTER_BY_MEDIUM,
            bundleOf(
                KEY_MEDIUM to viewModel.medium.value
            )
        )
    }
}