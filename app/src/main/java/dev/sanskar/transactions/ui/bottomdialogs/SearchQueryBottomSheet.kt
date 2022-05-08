package dev.sanskar.transactions.ui.bottomdialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.sanskar.transactions.KEY_SEARCH
import dev.sanskar.transactions.databinding.FragmentSearchQueryBottomSheetBinding
import dev.sanskar.transactions.text
import dev.sanskar.transactions.ui.home.MainViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchQueryBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSearchQueryBottomSheetBinding

    private val model by activityViewModels<MainViewModel>()

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

        binding.buttonSearch.setOnClickListener {
            val searchQuery = binding.textFieldSearchQuery.text
            if (searchQuery.isEmpty()) {
                binding.textFieldSearchQuery.error = "Empty Query!"
            } else {
                model.setSearchQuery(searchQuery)
                setFragmentResult(KEY_SEARCH, bundleOf(KEY_SEARCH to searchQuery))
                findNavController().popBackStack()
            }
        }
    }
}