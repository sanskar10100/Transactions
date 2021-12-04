package dev.sanskar.transactions.ui.home

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.sanskar.transactions.R
import dev.sanskar.transactions.data.Transaction
import dev.sanskar.transactions.databinding.LayoutTransactionBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionsListAdapter(val context: Context) : ListAdapter<Transaction, TransactionsListAdapter.ViewHolder>(TransactionDiffCallback()) {

    class ViewHolder(val binding: LayoutTransactionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = getItem(position)
        with (holder.binding) {
            textViewAmount.text = "₹${transaction.amount}"
            textViewDescription.text = transaction.description
            textViewTimestamp.text =  SimpleDateFormat("dd/MM/yy hh:mm", Locale.ENGLISH).format(Date(transaction.timestamp))

            if (transaction.isExpense) {
                root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.expense_red))
            } else {
                root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.income_green))
            }

            textViewSource.text = if (transaction.isDigital) "Digital" else "Cash"
        }
    }
}

class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }
}