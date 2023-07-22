package dev.sanskar.transactions.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val amount: Int,
    val timestamp: Long,
    val isExpense: Boolean,
    val description: String,
    val transactionType: Int
)