package dev.sanskar.transactions.ui.home

import dev.sanskar.transactions.data.*

data class FilterState(
    val filterAmountChoice: FilterByAmountChoices = FilterByAmountChoices.UNSPECIFIED,
    val filterAmountValue: Int = -1,
    val filterTypeChoice: FilterByTypeChoices = FilterByTypeChoices.UNSPECIFIED,
    val filterMediumChoice: FilterByMediumChoices = FilterByMediumChoices.UNSPECIFIED,
    val sortChoice: SortByChoices = SortByChoices.UNSPECIFIED,
    val searchChoice: SearchChoices = SearchChoices.UNSPECIFIED,
    val searchQuery: String = "",
    val filterTimeChoice: FilterByTimeChoices = FilterByTimeChoices.UNSPECIFIED,
    val filterFromTime: Long = -1L,
    val filterToTime: Long = -1L
)
