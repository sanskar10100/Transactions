package dev.sanskar.transactions.data

import androidx.sqlite.db.SimpleSQLiteQuery

enum class FilterByAmountChoices {
    GREATER_THAN,
    LESSER_THAN,
    UNSPECIFIED
}

enum class FilterByTypeChoices {
    INCOME,
    EXPENSE,
    UNSPECIFIED
}

enum class FilterByMediumChoices {
    CASH,
    DIGITAL,
    UNSPECIFIED
}

enum class FilterByTimeChoices {
    SPECIFIED,
    UNSPECIFIED
}

enum class SortByChoices {
    AMOUNT_HIGHEST_FIRST,
    AMOUNT_LOWEST_FIRST,
    TIME_EARLIEST_FIRST,
    TIME_NEWEST_FIRST,
    UNSPECIFIED
}

class QueryBuilder {

    private var filterAmountChoice = FilterByAmountChoices.UNSPECIFIED
    private var filterAmountValue = 0

    private var filterTypeChoice = FilterByTypeChoices.UNSPECIFIED
    private var filterMediumChoice = FilterByMediumChoices.UNSPECIFIED
    private var filterTimeChoice = FilterByTimeChoices.UNSPECIFIED
    private var fromTime = 0L
    private var toTime = 0L
    private var filterCount = 0

    private var sortChoice = SortByChoices.UNSPECIFIED

    fun setFilterAmount(filterAmountChoice: FilterByAmountChoices, amount: Int): QueryBuilder {
        this.filterAmountChoice = filterAmountChoice
        this.filterAmountValue = amount
        filterCount++
        return this
    }

    fun setFilterType(filterTypeChoice: FilterByTypeChoices): QueryBuilder {
        this.filterTypeChoice = filterTypeChoice
        filterCount++
        return this
    }

    fun setFilterMedium(filterMediumChoice: FilterByMediumChoices): QueryBuilder {
        this.filterMediumChoice = filterMediumChoice
        filterCount++
        return this
    }

//    fun setFilterTime(filterTimeChoice: FilterByTimeChoices, fromTime: Long, toTime: Long): QueryBuilder {
//        this.filterTimeChoice = filterTimeChoice
//        this.fromTime = fromTime
//        this.toTime = toTime
//        filterCount++
//        return this
//    }

    fun setSortingChoice(sortChoice: SortByChoices): QueryBuilder {
        this.sortChoice = sortChoice
        return this
    }

    private var previousFilterExists = false

    fun build(): SimpleSQLiteQuery {
        val query = StringBuilder("SELECT * FROM `transactions`")
        if (filterCount > 0) {
            query.append(" WHERE")
        }

        // Amount filter
        if (filterAmountChoice != FilterByAmountChoices.UNSPECIFIED) {
            query.append(" amount")
            val operator: String = when (filterAmountChoice) {
                FilterByAmountChoices.GREATER_THAN -> " >="
                FilterByAmountChoices.LESSER_THAN -> " <="
                else -> ""
            }
            query.append(operator)
            query.append(" $filterAmountValue")
            previousFilterExists = true
        }
        // Type filter
        if (filterTypeChoice != FilterByTypeChoices.UNSPECIFIED) {
            if (previousFilterExists) query.append(" AND")
            query.append(" isExpense")
            val typeIsExpense = when (filterTypeChoice) {
                FilterByTypeChoices.EXPENSE -> "= 1"
                FilterByTypeChoices.INCOME -> "= 0"
                else -> ""
            }
            query.append(" $typeIsExpense")
            previousFilterExists = true
        }

        // Medium filter
        if (filterMediumChoice != FilterByMediumChoices.UNSPECIFIED) {
            if (previousFilterExists) query.append(" AND")
            query.append(" isDigital")
            val mediumIsDigital = when (filterMediumChoice) {
                FilterByMediumChoices.DIGITAL -> "= 1"
                FilterByMediumChoices.CASH -> "= 0"
                else -> ""
            }
            query.append(" $mediumIsDigital")
            previousFilterExists = true
        }

        // Time filter
//        if (filterTimeChoice != FilterByTimeChoices.UNSPECIFIED) {
//            if (previousFilterExists) query.append(" AND")
//            query.append(" timestamp BETWEEN $fromTime AND $toTime")
//        }

        // Sort
        if (sortChoice != SortByChoices.UNSPECIFIED) {
            query.append(" SORT BY")
            val sort = when(sortChoice) {
                SortByChoices.AMOUNT_HIGHEST_FIRST -> "amount DESC"
                SortByChoices.AMOUNT_LOWEST_FIRST -> "amount ASC"
                SortByChoices.TIME_NEWEST_FIRST -> "timestamp DESC"
                SortByChoices.TIME_EARLIEST_FIRST -> "timestamp ASC"
                else -> ""
            }
            query.append(" $sort")
        }

        return SimpleSQLiteQuery(query.toString())
    }
}