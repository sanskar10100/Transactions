package dev.sanskar.transactions.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.sanskar.transactions.data.Transaction
import dev.sanskar.transactions.databinding.LayoutTransactionBinding
import java.util.*

class TransactionsListAdapter : ListAdapter<Transaction, TransactionsListAdapter.ViewHolder>(TransactionDiffCallback()) {

    class ViewHolder(val binding: LayoutTransactionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = getItem(position)
        with (holder.binding) {
            textViewAmount.text = transaction.amount.toString()
            textViewDescription.text = transaction.description
            textViewTimestamp.text = Date(transaction.timestamp * 1000).toString()
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