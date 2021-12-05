package dev.sanskar.transactions.ui.add

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private val args: TimePickerFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(activity, this, args.hour, args.minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        setFragmentResult("timePicker", bundleOf("hour" to hourOfDay, "minute" to minute))
    }
}