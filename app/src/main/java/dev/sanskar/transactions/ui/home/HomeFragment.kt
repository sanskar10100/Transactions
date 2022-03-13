package dev.sanskar.transactions.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.sanskar.transactions.*
import dev.sanskar.transactions.data.FilterByMediumChoices
import dev.sanskar.transactions.data.FilterByTypeChoices
import dev.sanskar.transactions.data.SortByChoices
import dev.sanskar.transactions.data.Transaction
import dev.sanskar.transactions.databinding.FragmentHomeBinding
import dev.sanskar.transactions.ui.model.MainViewModel
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig

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
            R.id.action_clear_transactions -> clearAllTransactionsDialog()
            R.id.action_dashboard -> {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToDashboardFragment())
            }
            R.id.action_exchange_medium -> {
                findNavController().navigate(R.id.action_homeFragment_to_mediumExchangeFragment)
            }
            R.id.action_send_feedback -> sendFeedbackDialog()
            R.id.action_clear_all_filters -> clearFilters()
            R.id.action_search -> findNavController().navigate(R.id.action_homeFragment_to_searchQueryBottomSheet)
        }

        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listTransactions.adapter = adapter
        setupRecyclerViewSwipe()
        onboard()

        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addTransactionFragment)
        }

        model.transactions.observe(viewLifecycleOwner) { onNewTransactionListReceived(it) }

        setChipListeners()

        // Delete event received from AddTransactionFragment
        setFragmentResultListener(KEY_DELETE_REQUEST) { _, bundle ->
            val id = bundle.getInt(KEY_DELETE_TRANSACTION_ID)
            model.deleteTransaction(id)
            binding.root.shortSnackbarWithUndo("Transaction Deleted!", model::undoTransactionDelete)
        }
    }

    /**
     * Sets swipe to delete action on recyclerview items.
     * Swiping on any item from the right results in the item being deleted
     */
    private fun setupRecyclerViewSwipe() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                model.deleteTransactionByPosition(viewHolder.adapterPosition)
                binding.root.shortSnackbarWithUndo("Transaction deleted!", model::undoTransactionDelete)
            }
        }).attachToRecyclerView(binding.listTransactions)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val transactionToEdit = model.transactions.value?.get(viewHolder.adapterPosition)
                if (transactionToEdit != null) findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToAddTransactionFragment(transactionToEdit.id)
                )
            }
        }).attachToRecyclerView(binding.listTransactions)
    }

    /**
     * Set chip titles.
     * TODO fix through LiveData
     */
    override fun onResume() {
        super.onResume()
        setChipTitles()
    }

    /**
     * Called whenever a new transactions list is received from the VM.
     * If the transactions are empty (result of a filter or new install), then the empty lottie view is shown.
     */
    private fun onNewTransactionListReceived(transactions: List<Transaction>) {
        if (transactions.isNullOrEmpty()) {
            binding.listTransactions.visibility = View.GONE
            binding.lottieEmpty.visibility = View.VISIBLE
            return
        } else {
            binding.listTransactions.visibility = View.VISIBLE
            binding.lottieEmpty.visibility = View.GONE
            adapter.submitList(transactions)
            binding.textViewCashBalance.text = "₹${model.getCashBalance()}"
            binding.textViewDigitalBalance.text = "₹${model.getDigitalBalance()}"
        }
    }

    /**
     * Resets chip titles to their default states
     */
    private fun setChipTitles() {
        binding.chipSort.text = MainViewModel.QueryConfig.sortChoice.readableString
        binding.chipFilterType.text = MainViewModel.QueryConfig.filterTypeChoice.readableString
        binding.chipFilterMedium.text = MainViewModel.QueryConfig.filterMediumChoice.readableString
        binding.chipFilterAmount.text = MainViewModel.QueryConfig.filterAmountChoice.readableString
    }

    private fun setChipListeners() {
        // Sort by Chip
        binding.chipSort.setOnClickListener {
            findNavController().navigate(
                generateOptionsDirection(
                    SortByChoices.values().map {
                        it.readableString
                    }.toTypedArray(), SORT_REQUEST_KEY, MainViewModel.QueryConfig.sortChoice.ordinal, "Sort By"
                )
            )
            setFragmentResultListener(SORT_REQUEST_KEY) { _, bundle ->
                val selected = bundle.getInt(KEY_SELECTED_OPTION_INDEX)
                model.setSortMethod(selected)
                (it as Chip).text = MainViewModel.QueryConfig.sortChoice.readableString
            }
        }

        // Filter by Type Chip
        binding.chipFilterType.setOnClickListener {
            findNavController().navigate(
                generateOptionsDirection(
                    FilterByTypeChoices.values().map {
                        it.readableString
                    }.toTypedArray(), KEY_FILTER_BY_TYPE, MainViewModel.QueryConfig.filterTypeChoice.ordinal, "Filter by Type"
                )
            )
            setFragmentResultListener(KEY_FILTER_BY_TYPE) { _, bundle ->
                val selected = bundle.getInt(KEY_SELECTED_OPTION_INDEX)
                model.setFilterType(selected)
                (it as Chip).text = MainViewModel.QueryConfig.filterTypeChoice.readableString
            }
        }

        // Filter by Medium Chip
        binding.chipFilterMedium.setOnClickListener {
            findNavController().navigate(
                generateOptionsDirection(
                    FilterByMediumChoices.values().map {
                        it.readableString
                    }.toTypedArray(), KEY_FILTER_BY_MEDIUM, MainViewModel.QueryConfig.filterMediumChoice.ordinal, "Filter by Medium"
                )
            )
            setFragmentResultListener(KEY_FILTER_BY_MEDIUM) { _, bundle ->
                val selected = bundle.getInt(KEY_SELECTED_OPTION_INDEX)
                model.setFilterMedium(selected)
                (it as Chip).text = MainViewModel.QueryConfig.filterMediumChoice.readableString
            }
        }

        // Filter by amount chip
        binding.chipFilterAmount.setOnClickListener {
            val directions = HomeFragmentDirections.actionHomeFragmentToAmountFilterBottomSheet(
                MainViewModel.QueryConfig.filterAmountValue,
                MainViewModel.QueryConfig.filterAmountChoice.ordinal
            )
            findNavController().navigate(directions)
            setFragmentResultListener(KEY_FILTER_BY_AMOUNT) { _, bundle ->
                val amount = bundle.getInt(KEY_AMOUNT)
                val index = bundle.getInt(KEY_SELECTED_OPTION_INDEX)
                model.setFilterAmount(amount, index)
                (it as Chip).text = if (amount != -1)
                    "${MainViewModel.QueryConfig.filterAmountChoice.readableString} ${MainViewModel.QueryConfig.filterAmountValue}"
                    else MainViewModel.QueryConfig.filterAmountChoice.readableString
            }
        }
    }

    /**
     * Returns generated directions for the OptionsBottomSheetDialogFragment
     */
    private fun generateOptionsDirection(options: Array<String>, key: String, selectedIndex: Int, title: String): NavDirections {
        return HomeFragmentDirections.actionHomeFragmentToOptionsBottomSheet(options, key, selectedIndex, title)
    }

    /**
     * Prompts for clearing all transactions
     */
    private fun clearAllTransactionsDialog() {
        // Clear transactions on dialog confirmation
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Are you sure?")
            .setMessage("Are you sure you want to delete all of your transactions records? This action is irreversible.")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                // Re-prompt
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Are you really sure?")
                    .setMessage("THIS ACTION IS IRREVERSIBLE!!")
                    .setPositiveButton("Delete Everything") { _, _ ->
                        model.clearAllTransactions()
                        binding.root.shortSnackbar("Deleted Everything!")
                    }
                    .setNegativeButton("Cancel") {_, _ -> }
                    .show()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Shows a feedback dialog with options for sending feedback through
     * Gmail or GitHub
     */
    private fun sendFeedbackDialog() {
        findNavController().navigate(R.id.action_homeFragment_to_feedbackBottomSheet)
    }

    /**
     * Clears all applied filters and resets the chip text
     */
    private fun clearFilters() {
        model.resetQueryConfig()
        setChipTitles()
    }

    /**
     * Shows a bunch of showcase views to let user know how to use the app
     */
    private fun onboard() {
        MaterialShowcaseSequence(requireActivity(), "0000").apply {
            setConfig(ShowcaseConfig().also { it.delay = 500 })
            addSequenceItem(MaterialShowcaseView.Builder(requireActivity())
                .setTarget(binding.fabAddTransaction)
                .setSkipText("Skip")
                .setDismissText("Got It")
                .setContentText("Click here to add a new transactions")
                .withRectangleShape()
                .build())
            addSequenceItem(binding.textViewCashBalance, "You can see your current cash balance here", "Got it")
            addSequenceItem(binding.textViewDigitalBalance, "You can see your current digital balance here", "Got it")
            addSequenceItem(binding.chipSort, "Sort your transactions by clicking here", "Got it")
            addSequenceItem(binding.chipFilterType, "Click this to filter your transactions by type or swipe right to see more options", "Got it")
            start()
        }
    }
}