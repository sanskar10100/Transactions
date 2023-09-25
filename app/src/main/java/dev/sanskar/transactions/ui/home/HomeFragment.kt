package dev.sanskar.transactions.ui.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import dev.sanskar.transactions.*
import dev.sanskar.transactions.data.*
import dev.sanskar.transactions.databinding.FragmentHomeBinding
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val model by activityViewModels<MainViewModel>()
    private val adapter by lazy {
        TransactionsListAdapter(requireContext())
    }

    private val exportToCsvIntent = registerForActivityResult(StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val uri = it.data?.data
            if (uri != null) {
                model.exportCsv(uri)
            } else {
                binding.root.shortSnackbar("Error exporting CSV")
            }
        }
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
                findNavController().navigate(R.id.action_homeFragment_to_dashboardFragment)
            }
            R.id.action_exchange_medium -> {
                findNavController().navigate(R.id.action_homeFragment_to_mediumExchangeFragment)
            }
            R.id.action_send_feedback -> showFeedbackSheet()
            R.id.action_clear_all_filters -> {
                model.resetFilters()
                binding.root.shortSnackbar("Filters cleared")
            }
            R.id.action_notifications -> findNavController().navigate(R.id.action_homeFragment_to_notificationsBottomSheet)
            R.id.action_export_csv -> exportToCsvIntent.launch(
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/csv"
                    putExtra(Intent.EXTRA_TITLE, "transaction_${System.currentTimeMillis()}.csv")
                })

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

        lifecycleScope.launch {
            model.message.collect {
                binding.root.shortSnackbar(it)
            }
        }

        initChipClickListeners()
        initFilterObservers()
        initFragmentResultListeners()
        initListUpdateListener()
        initScrollUpFabListener()
    }

    private fun initScrollUpFabListener() {
        binding.listTransactions.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    binding.fabScrollToTop.apply {
                        setOnClickListener { recyclerView.smoothScrollToPosition(0) }
                        show()
                    }
                }
                if (!binding.listTransactions.canScrollVertically(-1)) {
                    binding.fabScrollToTop.hideWithAnimation()
                }
            }
        })
    }

    private fun initFragmentResultListeners() {
        // Delete event received from AddTransactionFragment
        setFragmentResultListener(KEY_DELETE_REQUEST) { _, bundle ->
            val id = bundle.getInt(KEY_DELETE_TRANSACTION_ID)
            model.deleteTransaction(id)
            binding.root.shortSnackbarWithUndo("Transaction Deleted!", model::undoTransactionDelete)
        }

        // Add or edit event performed in [AddTransactionFragment], ask for review
        setFragmentResultListener(KEY_ADD_OR_UPDATE) { _, _ ->
            model.shouldAskForReview().collectWithLifecycle {
                if (it) {
                    log("Attempting to make review request")
                    val manager = ReviewManagerFactory.create(requireContext())
                    val request = manager.requestReviewFlow()
                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            log("Review request successfully attempted")
                            manager.launchReviewFlow(requireActivity(), task.result)
                        } else {
                            log("Failed to launch review popup with exception: ${task.exception?.message}")
                            findNavController().popBackStack()
                        }
                    }
                }
            }
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


    private fun initListUpdateListener() {
        model.transactions.collectWithLifecycle {
            if (it.isNullOrEmpty()) {
                binding.listTransactions.visibility = View.GONE
                binding.lottieEmpty.visibility = View.VISIBLE
            } else {
                binding.listTransactions.visibility = View.VISIBLE
                binding.lottieEmpty.visibility = View.GONE
                adapter.submitList(it)
            }
        }

        model.cashBalance.collectWithLifecycle {
            binding.textViewCashBalance.text = it.toString()
        }

        model.digitalBalance.collectWithLifecycle {
            binding.textViewDigitalBalance.text = it.toString()
        }

        model.creditBalance.collectWithLifecycle {
            binding.textViewCreditBalance.text = it.toString()
        }
    }

    /**
     * Resets chip titles to their default states
     */
    private fun initFilterObservers() {
        model.filterState.collectWithLifecycle {
            binding.chipSort.text = it.sortChoice.readableString
            binding.chipSearch.text = if (it.searchChoice == SearchChoices.UNSPECIFIED) {
                it.searchChoice.readableString
            } else {
                "${it.searchChoice.readableString} ${it.searchQuery}"
            }
            binding.chipFilterMedium.text = it.filterMediumChoice.toString()
            binding.chipFilterType.text = it.filterTypeChoice.readableString
            binding.chipFilterAmount.text = if (it.filterAmountChoice == FilterByAmountChoices.UNSPECIFIED) {
                it.filterAmountChoice.readableString
            } else {
                "${it.filterAmountChoice.readableString} ${it.filterAmountValue}"
            }
            binding.chipFilterTime.text = if (it.filterTimeChoice == FilterByTimeChoices.UNSPECIFIED) {
                it.filterTimeChoice.readableString
            } else {
                "${it.filterFromTime.asFormattedDateTime()}- ${it.filterToTime.asFormattedDateTime()}"
            }
        }

        val backPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            model.resetFilters()
        }

        model.backClearsFilter.collectWithLifecycle {
            backPressedCallback.isEnabled = it
        }
    }

    private fun initChipClickListeners() {
        // Sort by Chip
        binding.chipSort.setOnClickListener {
            findNavController().navigate(
                generateOptionsDirection(
                    SortByChoices.values().map {
                        it.readableString
                    }.toTypedArray(), SORT_REQUEST_KEY, model.filterState.value.sortChoice.ordinal, "Sort By"
                )
            )
            setFragmentResultListener(SORT_REQUEST_KEY) { _, bundle ->
                val selected = bundle.getInt(KEY_SELECTED_OPTION_INDEX)
                model.setSortMethod(selected)
            }
        }

        // Filter by Type Chip
        binding.chipFilterType.setOnClickListener {
            findNavController().navigate(
                generateOptionsDirection(
                    FilterByTypeChoices.values().map {
                        it.readableString
                    }.toTypedArray(), KEY_FILTER_BY_TYPE, model.filterState.value.filterTypeChoice.ordinal, "Filter by Type"
                )
            )
            setFragmentResultListener(KEY_FILTER_BY_TYPE) { _, bundle ->
                val selected = bundle.getInt(KEY_SELECTED_OPTION_INDEX)
                model.setFilterType(selected)
            }
        }

        // Filter by Medium Chip
        binding.chipFilterMedium.setOnClickListener {
            setFragmentResultListener(KEY_FILTER_BY_MEDIUM) { _, bundle ->
                val selectedMediums = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getSerializable(KEY_MEDIUM, FilterByMediumChoices::class.java)
                } else {
                    bundle.getSerializable(KEY_MEDIUM) as FilterByMediumChoices
                }
                model.setFilterMedium(selectedMediums!!)
            }

            val directions = HomeFragmentDirections.actionHomeFragmentToMediumFilterBottomSheet(model.filterState.value.filterMediumChoice)
            findNavController().navigate(directions)
        }

        // Filter by amount chip
        binding.chipFilterAmount.setOnClickListener {
            val directions = HomeFragmentDirections.actionHomeFragmentToAmountFilterBottomSheet(
                model.filterState.value.filterAmountValue,
                model.filterState.value.filterAmountChoice.ordinal
            )
            findNavController().navigate(directions)
            setFragmentResultListener(KEY_FILTER_BY_AMOUNT) { _, bundle ->
                val amount = bundle.getInt(KEY_AMOUNT)
                val index = bundle.getInt(KEY_SELECTED_OPTION_INDEX)
                model.setFilterAmount(amount, index)
            }
        }

        binding.chipSearch.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSearchQueryBottomSheet())
            setFragmentResultListener(KEY_SEARCH) { _, bundle ->
                model.setSearchQuery(bundle.getString(KEY_SEARCH) ?: "")
            }
        }

        binding.chipFilterTime.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select date range")
                .setSelection(
                    Pair(
                        MaterialDatePicker.thisMonthInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds()
                    )
                )
                .build()
            dateRangePicker.addOnPositiveButtonClickListener {
                // For some reason selecting 1 and 8 gives us time between 5:30 AM on these dates,
                // so we adjust the hour and minute ourself
                val from = Calendar.getInstance().run {
                    timeInMillis = it.first
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    timeInMillis
                }

                val to = Calendar.getInstance().run {
                    timeInMillis = it.second
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    timeInMillis
                }
                model.setFilterTime(from, to)
            }
            dateRangePicker.show(childFragmentManager, "dateRangePicker")
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
    private fun showFeedbackSheet() {
        findNavController().navigate(R.id.action_homeFragment_to_feedbackBottomSheet)
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
            addSequenceItem(binding.textViewCreditBalance, "You can see your current credit balance here", "Got it")
            addSequenceItem(binding.chipSearch, "Click this to search your transactions", "Got it")
            addSequenceItem(binding.chipSort, "Sort your transactions by clicking here.\nSwipe left to see more options!", "Got it")
            start()
        }
    }
}