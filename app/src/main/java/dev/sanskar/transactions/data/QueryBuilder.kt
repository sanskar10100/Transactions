package dev.sanskar.transactions.data

import androidx.sqlite.db.SimpleSQLiteQuery

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

enum class FilterByMediumChoices(val readableString: String) {
    CASH("Cash Only"),
    DIGITAL("Digital Only"),
    UNSPECIFIED("Cash and Digital")
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
    TIME_EARLIEST_FIRST("Earliest Transaction First"),
    TIME_NEWEST_FIRST("Latest Transaction First"),
    UNSPECIFIED("Default Order")
}

class QueryBuilder {

    private var filterAmountChoice = FilterByAmountChoices.UNSPECIFIED
    private var filterAmountValue = 0

    private var filterTypeChoice = FilterByTypeChoices.UNSPECIFIED
    private var filterMediumChoice = FilterByMediumChoices.UNSPECIFIED
    private var filterTimeChoice = FilterByTimeChoices.UNSPECIFIED
    private var fromTime = 0L
    private var toTime = 0L
    private var filterEnabled = false

    private var sortChoice = SortByChoices.UNSPECIFIED

    private var searchChoice = SearchChoices.UNSPECIFIED
    private var searchQuery = ""

    fun setFilterAmount(filterAmountChoice: FilterByAmountChoices, amount: Int): QueryBuilder {
        if (filterAmountChoice != FilterByAmountChoices.UNSPECIFIED) {
            this.filterAmountChoice = filterAmountChoice
            this.filterAmountValue = amount
            this.filterEnabled = true
        }
        return this
    }

    fun setFilterType(filterTypeChoice: FilterByTypeChoices): QueryBuilder {
        if (filterTypeChoice != FilterByTypeChoices.UNSPECIFIED) {
            this.filterTypeChoice = filterTypeChoice
            this.filterEnabled = true
        }
        return this
    }

    fun setFilterMedium(filterMediumChoice: FilterByMediumChoices): QueryBuilder {
        if (filterMediumChoice != FilterByMediumChoices.UNSPECIFIED) {
            this.filterMediumChoice = filterMediumChoice
            this.filterEnabled = true
        }
        return this
    }

    fun setFilterSearch(searchChoice: SearchChoices, searchQuery: String): QueryBuilder {
        if (searchChoice != SearchChoices.UNSPECIFIED) {
            this.searchChoice = searchChoice
            this.searchQuery = searchQuery.lowercase()
            this.filterEnabled = true
        }
        return this
    }

    fun setFilterTime(filterTimeChoice: FilterByTimeChoices, fromTime: Long, toTime: Long): QueryBuilder {
        if (filterTimeChoice != FilterByTimeChoices.UNSPECIFIED) {
            this.filterTimeChoice = filterTimeChoice
            this.fromTime = fromTime
            this.toTime = toTime
            this.filterEnabled = true
        }
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
        val query = StringBuilder("SELECT * FROM `transaction`")
        if (filterEnabled) {
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

        if (searchChoice != SearchChoices.UNSPECIFIED) {
            if (previousFilterExists) query.append(" AND")
            query.append(" description LIKE '%$searchQuery%'")
            previousFilterExists = true
        }

        if (filterTimeChoice != FilterByTimeChoices.UNSPECIFIED) {
            if (previousFilterExists) query.append(" AND")
            query.append(" timestamp BETWEEN $fromTime AND $toTime")
            previousFilterExists = true
        }

        // Time filter
//        if (filterTimeChoice != FilterByTimeChoices.UNSPECIFIED) {
//            if (previousFilterExists) query.append(" AND")
//            query.append(" timestamp BETWEEN $fromTime AND $toTime")
//        }

        // Sort
        if (sortChoice != SortByChoices.UNSPECIFIED) {
            query.append(" ORDER BY")
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