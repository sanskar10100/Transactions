package dev.sanskar.transactions.ui.home

import dev.sanskar.transactions.data.*

data class FilterState(
    val filterAmountChoice: FilterByAmountChoices = FilterByAmountChoices.UNSPECIFIED,
    val filterAmountValue: Int = -1,
    val filterTypeChoice: FilterByTypeChoices = FilterByTypeChoices.UNSPECIFIED,
    val filterMediumChoice: FilterByMediumChoices = FilterByMediumChoices(true, true, true),
    val sortChoice: SortByChoices = SortByChoices.UNSPECIFIED_TIME_EARLIEST_FIRST,
    val searchChoice: SearchChoices = SearchChoices.UNSPECIFIED,
    val searchQuery: String = "",
    val filterTimeChoice: FilterByTimeChoices = FilterByTimeChoices.UNSPECIFIED,
    val filterFromTime: Long = -1L,
    val filterToTime: Long = -1L
)

fun FilterState.areFiltersActive(): Boolean {
    return filterAmountChoice != FilterByAmountChoices.UNSPECIFIED ||
            filterTypeChoice != FilterByTypeChoices.UNSPECIFIED || !filterMediumChoice.cash || !filterMediumChoice.digital || !filterMediumChoice.credit || sortChoice != SortByChoices.UNSPECIFIED_TIME_EARLIEST_FIRST || searchChoice != SearchChoices.UNSPECIFIED || filterTimeChoice != FilterByTimeChoices.UNSPECIFIED
}
