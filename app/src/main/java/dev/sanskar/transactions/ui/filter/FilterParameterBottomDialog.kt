package dev.sanskar.transactions.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.sanskar.transactions.R
import dev.sanskar.transactions.databinding.FragmentFilterParameterBottomDialogBinding
import dev.sanskar.transactions.getText

private const val TAG = "FilterParameterBottomDi"

class FilterParameterBottomDialog : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentFilterParameterBottomDialogBinding
    private val model by activityViewModels<FilterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilterParameterBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFilter.setOnClickListener {
            if (binding.textFieldFilterAmount.getText().isEmpty()) {
                binding.textFieldFilterAmount.error = "Please enter a valid amount"
            } else {
                val amount = binding.textFieldFilterAmount.getText().toInt()
                model.filterOnAmount(amount)
                findNavController().navigate(R.id.action_filterParameterBottomDialog_to_filterResultFragment)
            }
        }
    }
}