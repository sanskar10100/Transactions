package dev.sanskar.transactions.ui.home

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.sanskar.transactions.R
import dev.sanskar.transactions.databinding.FragmentHomeBinding
import dev.sanskar.transactions.ui.model.MainViewModel
import kotlin.math.log

private const val TAG = "HomeFragment"
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val model by activityViewModels<MainViewModel>()
    private val adapter by lazy {
        TransactionsListAdapter(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_options_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear_transactions -> {
                // Clear transactions on dialog confirmation
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Are you sure?")
                    .setMessage("Are you sure you want to delete all transactions to date?")
                    .setPositiveButton("Yes") { _, _ ->
                        model.clearTransactions()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            R.id.action_dashboard -> {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToDashboardFragment())
            }
            R.id.action_exchange_medium -> {
                findNavController().navigate(R.id.action_homeFragment_to_mediumExchangeFragment)
            }
            R.id.action_send_feedback -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setItems(arrayOf("GitHub", "Gmail")) { _, which ->
                        Log.d(TAG, "onOptionsItemSelected: $which")
                        when (which) {
                            0 -> {
                                Log.d(
                                    TAG,
                                    "onOptionsItemSelected: Attempting to create and launch a custom tab"
                                )
                            CustomTabsIntent.Builder()
                                .build()
                                .launchUrl(requireContext(), Uri.parse("https://github.com/sanskar10100/Transactions/issues/new"))
                            }
                            1 -> {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:")
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf("sanskar10100@gmail.com"))
                                    putExtra(Intent.EXTRA_SUBJECT, "Feedback - Transactions")
                                }
                                startActivity(Intent.createChooser(intent, "Send Feedback"))
                            }
                        }
                    }
                    .create()
                    .show()
            }
            R.id.action_filter -> {
                findNavController().navigate(R.id.action_homeFragment_to_filterParameterBottomDialog)
            }
        }

        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAppUpdate()
        binding.listTransactions.adapter = adapter

        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addTransactionFragment)
        }

        model.transactions.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.textViewCashBalance.text = "₹${model.getCashBalance()}"
            binding.textViewDigitalBalance.text = "₹${model.getDigitalBalance()}"
        }
    }

    private fun checkAppUpdate() {
        AppUpdater(context)
            .setUpdateFrom(UpdateFrom.GITHUB)
            .setGitHubUserAndRepo("sanskar10100", "Transactions")
            .setDisplay(Display.DIALOG)
            .start()
    }
}