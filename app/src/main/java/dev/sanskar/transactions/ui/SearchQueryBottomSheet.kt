package dev.sanskar.transactions.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.sanskar.transactions.databinding.FragmentSearchQueryBottomSheetBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchQueryBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSearchQueryBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchQueryBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textFieldSearchQuery.requestFocus()
        GlobalScope.launch {
            delay(200L)

            val inputMethodManager: InputMethodManager =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInputFromWindow(
                binding.textFieldSearchQuery.editText?.applicationWindowToken,
                InputMethodManager.SHOW_IMPLICIT, 0
            )
        }
    }
}