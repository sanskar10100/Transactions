package dev.sanskar.transactions.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "amount") val amount: Int,
    @ColumnInfo(name = "added_at") val timestamp: Long,
    @ColumnInfo(name = "is_expense") val isExpense: Boolean,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "is_digital") val isDigital: Boolean
)