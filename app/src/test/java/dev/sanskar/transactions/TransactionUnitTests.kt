package dev.sanskar.transactions

import dev.sanskar.transactions.data.*
import dev.sanskar.transactions.ui.home.FilterState
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TransactionUnitTests {
    @Test
    fun query_filter_amount_greater_and_type_income() {
        val filter = FilterState(
            filterAmountChoice = FilterByAmountChoices.GREATER_THAN,
            filterAmountValue = 100,
            filterTypeChoice = FilterByTypeChoices.INCOME
        )
        assertEquals("SELECT * FROM `transaction` WHERE amount >= 100 AND isExpense = 0 ORDER BY timestamp ASC", buildQuery(filter).sql)
    }

    @Test
    fun query_filter_amount_greater_and_type_income_and_medium_digital() {
        val filter = FilterState(
            filterAmountChoice = FilterByAmountChoices.GREATER_THAN,
            filterAmountValue = 100,
            filterTypeChoice = FilterByTypeChoices.INCOME,
            filterMediumChoice = FilterByMediumChoices.DIGITAL
        )
        assertEquals("SELECT * FROM `transaction` WHERE amount >= 100 AND isExpense = 0 AND isDigital = 1 ORDER BY timestamp ASC", buildQuery(filter).sql)
    }

    @Test
    fun query_filter_amount_greater_and_type_income_and_medium_digital_and_sort_by_time_oldest() {
        val filterState = FilterState(
            filterAmountChoice = FilterByAmountChoices.GREATER_THAN,
            filterAmountValue = 100,
            filterTypeChoice = FilterByTypeChoices.INCOME,
            filterMediumChoice = FilterByMediumChoices.DIGITAL,
            sortChoice = SortByChoices.UNSPECIFIED_TIME_EARLIEST_FIRST
        )
        assertEquals("SELECT * FROM `transaction` WHERE amount >= 100 AND isExpense = 0 AND isDigital = 1 ORDER BY timestamp ASC", buildQuery(filterState).sql)
    }

    @Test
    fun query_filter_type_income_only() {
        val filter = FilterState(
            filterTypeChoice = FilterByTypeChoices.INCOME
        )
        assertEquals("SELECT * FROM `transaction` WHERE isExpense = 0 ORDER BY timestamp ASC", buildQuery(filter).sql)
    }

    @Test
    fun query_filter_type_expense_only() {
        val filter = FilterState(
            filterTypeChoice = FilterByTypeChoices.EXPENSE
        )
        assertEquals("SELECT * FROM `transaction` WHERE isExpense = 1 ORDER BY timestamp ASC", buildQuery(filter).sql)
    }

    @Test
    fun query_medium_cash_only() {
        val filter = FilterState(
            filterMediumChoice = FilterByMediumChoices.CASH
        )
        assertEquals("SELECT * FROM `transaction` WHERE isDigital = 0 ORDER BY timestamp ASC", buildQuery(filter).sql)
    }

    @Test
    fun query_medium_digital_only() {
        val filter = FilterState(
            filterMediumChoice = FilterByMediumChoices.DIGITAL
        )
        assertEquals("SELECT * FROM `transaction` WHERE isDigital = 1 ORDER BY timestamp ASC", buildQuery(filter).sql)
    }

    @Test
    fun query_all() {
        val filter = FilterState()
        assertEquals("SELECT * FROM `transaction` ORDER BY timestamp ASC", buildQuery(filter).sql)
    }

    @Test
    fun query_sort_by_amount_highest() {
        val filter = FilterState(
            sortChoice = SortByChoices.AMOUNT_HIGHEST_FIRST
        )
        assertEquals("SELECT * FROM `transaction` ORDER BY amount DESC", buildQuery(filter).sql)
    }

    @Test
    fun query_sort_and_search() {
        val filter =FilterState(
            sortChoice = SortByChoices.AMOUNT_HIGHEST_FIRST,
            searchChoice = SearchChoices.SPECIFIED,
            searchQuery = "recharge"
        )
        assertEquals("SELECT * FROM `transaction` WHERE description LIKE '%recharge%' ORDER BY amount DESC", buildQuery(filter).sql)
    }

    @Test
    fun query_search_only() {
        val filter = FilterState(
            searchChoice = SearchChoices.SPECIFIED,
            searchQuery = "recharge"
        )
        assertEquals("SELECT * FROM `transaction` WHERE description LIKE '%recharge%' ORDER BY timestamp ASC", buildQuery(filter).sql)
    }

    @Test
    fun sort_and_time() {
        val filter = FilterState(
            sortChoice = SortByChoices.UNSPECIFIED_TIME_EARLIEST_FIRST,
            filterTimeChoice = FilterByTimeChoices.SPECIFIED,
            filterFromTime = 249808343289,
            filterToTime = 8439837973
        )
        assertEquals("SELECT * FROM `transaction` WHERE timestamp BETWEEN 249808343289 AND 8439837973 ORDER BY timestamp ASC", buildQuery(filter).sql)
    }

    @Test
    fun time_only() {
        val filter = FilterState(
            filterTimeChoice = FilterByTimeChoices.SPECIFIED,
            filterFromTime = 249808343289,
            filterToTime = 8439837973
        )
        assertEquals(
            "SELECT * FROM `transaction` WHERE timestamp BETWEEN 249808343289 AND 8439837973 ORDER BY timestamp ASC",
            buildQuery(filter).sql
        )
    }
}