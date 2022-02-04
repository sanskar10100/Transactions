package dev.sanskar.transactions.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.sanskar.transactions.KEY_SELECTED_VIEW_OPTION
import dev.sanskar.transactions.VIEW_OPTIONS_REQUEST_KEY
import dev.sanskar.transactions.databinding.FragmentViewByMediumOptionBinding

class ViewByMediumOptionsBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentViewByMediumOptionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewByMediumOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args by navArgs<ViewByMediumOptionsBottomSheetArgs>()

        when (args.selectedViewOption) {
            ViewByMediumOptions.CASH_ONLY -> binding.textViewOptionCashOnly.isPressed = true
            ViewByMediumOptions.DIGITAL_ONLY -> binding.textViewOptionDigitalOnly.isPressed = true
            ViewByMediumOptions.ALL -> binding.textViewOptionCashAndDigital.isPressed = true
        }

        binding.textViewOptionCashOnly.setOnClickListener {
            setSelectedOption(ViewByMediumOptions.CASH_ONLY)
        }

        binding.textViewOptionDigitalOnly.setOnClickListener {
            setSelectedOption(ViewByMediumOptions.DIGITAL_ONLY)
        }

        binding.textViewOptionCashAndDigital.setOnClickListener {
            setSelectedOption(ViewByMediumOptions.ALL)
        }
    }

    private fun setSelectedOption(option: ViewByMediumOptions) {
        setFragmentResult(VIEW_OPTIONS_REQUEST_KEY, bundleOf(KEY_SELECTED_VIEW_OPTION to option))
        dismiss()
    }
}