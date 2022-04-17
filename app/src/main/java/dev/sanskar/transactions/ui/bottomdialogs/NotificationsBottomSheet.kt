package dev.sanskar.transactions.ui.bottomdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import dev.sanskar.transactions.DEFAULT_REMINDER_HOUR
import dev.sanskar.transactions.DEFAULT_REMINDER_MINUTE
import dev.sanskar.transactions.databinding.FragmentNotificationsBottomSheetBinding
import dev.sanskar.transactions.get12HourTime
import dev.sanskar.transactions.ui.home.MainViewModel

@AndroidEntryPoint
class NotificationsBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentNotificationsBottomSheetBinding
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setInitial()

        binding.checkboxReminder.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Was previously removed by user, now user wants to re-enable.
                viewModel.scheduleReminderNotification(DEFAULT_REMINDER_HOUR, DEFAULT_REMINDER_MINUTE)
                binding.textViewReminderTime.isEnabled = true
                binding.textViewReminderTime.text = get12HourTime(DEFAULT_REMINDER_HOUR, DEFAULT_REMINDER_MINUTE)
            } else {
                viewModel.cancelReminderNotification()
                binding.textViewReminderTime.isEnabled = false
                binding.textViewReminderTime.text = "Not set"
            }
        }

        binding.textViewReminderTime.setOnClickListener {
            val time = viewModel.getReminderTime()

            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(time.first)
                .setMinute(time.second)
                .setTitleText("Select Reminder Time")
                .build()
            picker.show(parentFragmentManager, picker.toString())

            picker.addOnPositiveButtonClickListener {
                val hour = picker.hour
                val minute = picker.minute
                viewModel.scheduleReminderNotification(hour, minute)
                val newTime = viewModel.getReminderTime()
                binding.textViewReminderTime.text = get12HourTime(newTime.first, newTime.second)
            }
        }
    }

    private fun setInitial() {
        val setTime = viewModel.getReminderTime()
        if (setTime.first == -1 || setTime.second == -1 ) {
            binding.checkboxReminder.isChecked = false
            binding.textViewReminderTime.isEnabled = false
            binding.textViewReminderTime.text = "Not set"
        } else {
            binding.checkboxReminder.isChecked = true
            binding.textViewReminderTime.isEnabled = true
            binding.textViewReminderTime.text = get12HourTime(setTime.first, setTime.second)
        }
    }
}