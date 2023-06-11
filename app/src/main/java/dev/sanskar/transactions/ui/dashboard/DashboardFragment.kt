package dev.sanskar.transactions.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.sanskar.transactions.databinding.FragmentDashboardBinding

import com.github.mikephil.charting.formatter.PercentFormatter

import com.github.mikephil.charting.data.PieData

import com.github.mikephil.charting.data.PieDataSet

import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint
import dev.sanskar.transactions.R
import dev.sanskar.transactions.collectWithLifecycle

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private val model by viewModels<DashboardViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSourcePieChart()

        model.getExpensesSinceMidnight().collectWithLifecycle {
            binding.textViewExpensesToday.text = "Expenses Since Midnight: ₹$it"
        }

    }

    private fun setupSourcePieChart() {
        with (binding.chartPieChart) {
            val expense: MutableList<PieEntry> = mutableListOf(
                PieEntry((model.getDigitalExpense() / model.getTotalExpenses()) * 10, "Digital"),
                PieEntry((model.getCashExpense() / model.getTotalExpenses())  * 10, "Cash"),
            )

            val dataset = PieDataSet(expense, "Expenses by Source")
            dataset.colors = mutableListOf(ContextCompat.getColor(context, R.color.digital_blue), ContextCompat.getColor(context, R.color.cash_orange))

            this.data = PieData(dataset)
            this.data.setValueTextSize(14f)
            this.data.setValueTextColor(Color.WHITE)
            this.data.setValueFormatter(PercentFormatter(this))
            this.animateXY(2000, 2000)

            this.legend.isEnabled = false
            this.description.isEnabled = false
            this.centerText = "Expenses by Source"
            this.setCenterTextSize(16f)
            this.setUsePercentValues(true)
        }
    }
}