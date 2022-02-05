package dev.sanskar.transactions

import dev.sanskar.transactions.data.*
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
        val query = QueryBuilder()
            .setFilterAmount(FilterByAmountChoices.GREATER_THAN, 100)
            .setFilterType(FilterByTypeChoices.INCOME)
            .build()
        assertEquals("SELECT * FROM `transaction` WHERE amount >= 100 AND isExpense = 0", query.sql)
    }

    @Test
    fun query_filter_amount_greater_and_type_income_and_medium_digital() {
        val query = QueryBuilder()
            .setFilterAmount(FilterByAmountChoices.GREATER_THAN, 100)
            .setFilterType(FilterByTypeChoices.INCOME)
            .setFilterMedium(FilterByMediumChoices.DIGITAL)
            .build()
        assertEquals("SELECT * FROM `transaction` WHERE amount >= 100 AND isExpense = 0 AND isDigital = 1", query.sql)
    }

    @Test
    fun query_filter_amount_greater_and_type_income_and_medium_digital_and_sort_by_time_oldest() {
        val query = QueryBuilder()
            .setFilterAmount(FilterByAmountChoices.GREATER_THAN, 100)
            .setFilterType(FilterByTypeChoices.INCOME)
            .setFilterMedium(FilterByMediumChoices.DIGITAL)
            .setSortingChoice(SortByChoices.TIME_EARLIEST_FIRST)
            .build()
        assertEquals("SELECT * FROM `transaction` WHERE amount >= 100 AND isExpense = 0 AND isDigital = 1 ORDER BY timestamp ASC", query.sql)
    }

    @Test
    fun query_filter_type_income_only() {
        val query = QueryBuilder().setFilterType(FilterByTypeChoices.INCOME).build()
        assertEquals("SELECT * FROM `transaction` WHERE isExpense = 0", query.sql)
    }

    @Test
    fun query_filter_type_expense_only() {
        val query = QueryBuilder().setFilterType(FilterByTypeChoices.EXPENSE).build()
        assertEquals("SELECT * FROM `transaction` WHERE isExpense = 1", query.sql)
    }

    @Test
    fun query_medium_cash_only() {
        val query = QueryBuilder().setFilterMedium(FilterByMediumChoices.CASH).build()
        assertEquals("SELECT * FROM `transaction` WHERE isDigital = 0", query.sql)
    }

    @Test
    fun query_medium_digital_only() {
        val query = QueryBuilder().setFilterMedium(FilterByMediumChoices.DIGITAL).build()
        assertEquals("SELECT * FROM `transaction` WHERE isDigital = 1", query.sql)
    }

    @Test
    fun query_all() {
        val query = QueryBuilder().build()
        assertEquals("SELECT * FROM `transaction`", query.sql)
    }

    @Test
    fun query_sort_by_amount_highest() {
        val query = QueryBuilder()
            .setSortingChoice(SortByChoices.AMOUNT_HIGHEST_FIRST)
            .build()
        assertEquals("SELECT * FROM `transaction` ORDER BY amount DESC", query.sql)
    }
}