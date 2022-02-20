package dev.sanskar.transactions.ui.exchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.sanskar.transactions.databinding.FragmentMediumExchangeBinding
import dev.sanskar.transactions.text
import dev.sanskar.transactions.ui.add.AddViewModel

class MediumExchangeFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentMediumExchangeBinding
    private val model by viewModels<AddViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMediumExchangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chipCashSource.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.chipDigitalDestination.isChecked = true
        }
        binding.chipDigitalSource.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.chipCashDestination.isChecked = true
        }
        binding.chipCashDestination.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.chipDigitalSource.isChecked = true
        }
        binding.chipDigitalDestination.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.chipCashSource.isChecked = true
        }

        binding.chipCashSource.isChecked = true


        binding.buttonAdd.setOnClickListener {
            if (binding.textFieldAmount.text.isEmpty()) {
                binding.textFieldAmount.error = "Amount cannot be empty!"
                return@setOnClickListener
            } else {
                val amount = binding.textFieldAmount.text.toInt();
                if (binding.chipCashSource.isChecked) {
                    // Remove from cash and add to digital
                    model.addTransaction(amount, "Currency Medium Exchange", System.currentTimeMillis(), false, true)
                    model.addTransaction(amount, "Currency Medium Exchange", System.currentTimeMillis(), true, false)
                } else if (binding.chipDigitalSource.isChecked) {
                    // Remove from digital and add to cash
                    model.addTransaction(amount, "Currency Medium Exchange", System.currentTimeMillis(), true, true)
                    model.addTransaction(amount, "Currency Medium Exchange", System.currentTimeMillis(), false, false)
                }
                dialog?.dismiss()
            }
        }
    }
}