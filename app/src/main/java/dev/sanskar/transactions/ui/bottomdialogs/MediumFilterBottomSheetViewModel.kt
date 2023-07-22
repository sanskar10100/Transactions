package dev.sanskar.transactions.ui.bottomdialogs

import androidx.lifecycle.ViewModel
import dev.sanskar.transactions.data.FilterByMediumChoices
import kotlinx.coroutines.flow.MutableStateFlow

class MediumFilterBottomSheetViewModel : ViewModel() {
    val medium = MutableStateFlow(FilterByMediumChoices(true, true, true))

    fun initialize(selectedMediums: FilterByMediumChoices) {
        medium.value = selectedMediums
    }

    fun toggleCash(checked: Boolean) {
        medium.value = medium.value.copy(cash = checked)
    }

    fun toggleDigital(checked: Boolean) {
        medium.value = medium.value.copy(digital = checked)
    }

    fun toggleCredit(checked: Boolean) {
        medium.value = medium.value.copy(credit = checked)
    }

    fun reset() {
        medium.value = FilterByMediumChoices(true, true, true)
    }

    fun isAnyMediumSelected(): Boolean {
        return medium.value.cash || medium.value.digital || medium.value.credit
    }
}