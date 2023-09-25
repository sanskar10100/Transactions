package dev.sanskar.transactions.data

import androidx.sqlite.db.SimpleSQLiteQuery
import dev.sanskar.transactions.ui.home.FilterState
import dev.sanskar.transactions.ui.home.areFiltersActive
import java.io.Serializable

enum class FilterByAmountChoices(val readableString: String) {
    GREATER_THAN("Amount Greater Than"),
    LESSER_THAN("Amount Lesser Than"),
    UNSPECIFIED("Any Amount")
}

enum class FilterByTypeChoices(val readableString: String) {
    INCOME("Income Only"),
    EXPENSE("Expense Only"),
    UNSPECIFIED("Income and Expense")
}

data class FilterByMediumChoices(
    val cash: Boolean,
    val digital: Boolean,
    val credit: Boolean
): Serializable {
    override fun toString(): String {
        val choices = mutableListOf<String>()
        if (cash) { choices += "Cash" }
        if (digital) { choices += "Digital" }
        if (credit) { choices += "Credit" }
        return choices.joinToString(", ")
    }
}

enum class FilterByTimeChoices(val readableString: String) {
    SPECIFIED(""),
    UNSPECIFIED("Any time")
}

enum class SearchChoices(val readableString: String) {
    SPECIFIED("Searching For"),
    UNSPECIFIED("Search by Description")
}

enum class SortByChoices(val readableString: String) {
    AMOUNT_HIGHEST_FIRST("Highest Amount First"),
    AMOUNT_LOWEST_FIRST("Lowest Amount First"),
    UNSPECIFIED_TIME_EARLIEST_FIRST("Earliest Transaction First"),
    TIME_NEWEST_FIRST("Latest Transaction First"),
    INSERT_ORDER("Insert Order")
}

fun buildQuery(filter: FilterState): SimpleSQLiteQuery {
    var previousFilterExists = false
    val query = StringBuilder("SELECT * FROM `transaction`")
    if (filter.areFiltersActive()) {
        query.append(" WHERE")
    }

    // Amount filter
    if (filter.filterAmountChoice != FilterByAmountChoices.UNSPECIFIED) {
        query.append(" amount")
        val operator: String = when (filter.filterAmountChoice) {
            FilterByAmountChoices.GREATER_THAN -> " >="
            FilterByAmountChoices.LESSER_THAN -> " <="
            else -> ""
        }
        query.append(operator)
        query.append(" ${filter.filterAmountValue}")
        previousFilterExists = true
    }
    // Type filter
    if (filter.filterTypeChoice != FilterByTypeChoices.UNSPECIFIED) {
        if (previousFilterExists) query.append(" AND")
        query.append(" isExpense")
        val typeIsExpense = when (filter.filterTypeChoice) {
            FilterByTypeChoices.EXPENSE -> "= 1"
            FilterByTypeChoices.INCOME -> "= 0"
            else -> ""
        }
        query.append(" $typeIsExpense")
        previousFilterExists = true
    }

    // Medium filter. It is guaranteed that at least one medium will be true at all times.
    if (filter.filterMediumChoice != FilterByMediumChoices(true, true, true)) {
        if (previousFilterExists) query.append(" AND")
        query.append(" transactionType IN ")
        val list = mutableListOf<Int>()
        if (filter.filterMediumChoice.cash) list.add(0)
        if (filter.filterMediumChoice.digital) list.add(1)
        if (filter.filterMediumChoice.credit) list.add(2)
        query.append("(${list.joinToString()})")
        previousFilterExists = true
    }

    if (filter.searchChoice != SearchChoices.UNSPECIFIED) {
        if (previousFilterExists) query.append(" AND")
        query.append(" description LIKE '%${filter.searchQuery}%'")
        previousFilterExists = true
    }

    if (filter.filterTimeChoice != FilterByTimeChoices.UNSPECIFIED) {
        if (previousFilterExists) query.append(" AND")
        query.append(" timestamp BETWEEN ${filter.filterFromTime} AND ${filter.filterToTime}")
        previousFilterExists = true
    }

    // Sort
    if (filter.sortChoice != SortByChoices.INSERT_ORDER) {
        query.append(" ORDER BY")
        val sort = when(filter.sortChoice) {
            SortByChoices.AMOUNT_HIGHEST_FIRST -> "amount DESC"
            SortByChoices.AMOUNT_LOWEST_FIRST -> "amount ASC"
            SortByChoices.TIME_NEWEST_FIRST -> "timestamp DESC"
            SortByChoices.UNSPECIFIED_TIME_EARLIEST_FIRST -> "timestamp ASC"
            else -> ""
        }
        query.append(" $sort")
    }

    return SimpleSQLiteQuery(query.toString())
}