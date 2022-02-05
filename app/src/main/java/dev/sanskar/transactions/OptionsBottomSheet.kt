package dev.sanskar.transactions

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.sanskar.transactions.databinding.FragmentOptionsBottomSheetBinding

class OptionsBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentOptionsBottomSheetBinding
    private val args by navArgs<OptionsBottomSheetArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val options = args.options
        options.forEachIndexed { index, s ->
            val optionTextView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8)
                    setPadding(16)
                }
                text = s
                setTypeface(null, Typeface.BOLD)
                setBackgroundResource(R.drawable.selector_view_by_medium_option)
                textSize = 16f
                setOnClickListener {
                    optionIndexSelected(index)
                }
                if (index == args.selectedItemIndex) this.isPressed = true
            }
            binding.root.addView(optionTextView)
        }
    }

    private fun optionIndexSelected(optionIndex: Int) {
        setFragmentResult(args.requestKey, bundleOf(
            KEY_SELECTED_OPTION_INDEX to optionIndex,
            KEY_SELECTED_OPTION to args.options[optionIndex]
        ))
        dismiss()
    }
}