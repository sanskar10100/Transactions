package dev.sanskar.transactions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

const val KEY_SELECTED_OPTION = "selected_option"
const val KEY_SELECTED_OPTION_INDEX = "selected_option_index"
const val SORT_REQUEST_KEY = "sort_request"
const val KEY_FILTER_BY_TYPE = "key_filter_by_type"
const val KEY_FILTER_BY_MEDIUM = "key_filter_by_medium"
const val KEY_FILTER_BY_AMOUNT = "key_filter_by_amount"
const val KEY_AMOUNT = "key_amount"


const val KEY_DELETE_REQUEST = "key_delete_request"
const val KEY_DELETE_TRANSACTION_ID = "key_delete_transaction_id"

const val KEY_SEARCH = "key_search"

const val KEY_ADD_OR_UPDATE = "key_add_or_update"

const val ISSUE_URL = "https://github.com/sanskar10100/Transactions/issues/new"

const val TAG_REMINDER_WORKER = "reminder_worker"
const val SHARED_PREF_REMINDER_HOUR = "reminder_hour"
const val SHARED_PREF_REMINDER_MINUTE = "reminder_minute"
const val DEFAULT_REMINDER_HOUR = 22
const val DEFAULT_REMINDER_MINUTE = 0

fun Long.asFormattedDateTime() : String {
    return SimpleDateFormat(
        "dd/MM/yy hh:mm aaa",
        Locale.ENGLISH
    ).format(Date(this))
}

fun get12HourTime(hour: Int, minute: Int) : String {
    val minutesString = if (minute < 10) "0$minute" else "$minute"

    return "${if (hour > 12) hour - 12 else hour}:$minutesString ${if (hour > 12) "PM" else "AM"}"
}

var TextInputLayout.text: String
    get() {
        return this.editText?.text.toString() ?: ""
    }
    set(value) = this.editText?.setText(value)!!

fun View.shortSnackbar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}

fun Context.shortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun View.shortSnackbarWithUndo(message: String, onAction: () -> Unit) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        .setAction("UNDO") { onAction() }
        .show()
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.showWithAnimation(duration: Long = 300) = run {
    this.apply {
        alpha = 0f
        visibility = View.VISIBLE
        animate().alpha(1f).duration = duration
    }
}

fun View.hideWithAnimation(duration: Long = 300) = run {
    this.animate().alpha(0f).setDuration(duration).withEndAction { this.hide() }.start()
}

fun log(message: String) {
    if (BuildConfig.DEBUG || BuildConfig.BUILD_TYPE.lowercase().contains("debug"))
        Log.d("TransactionsDebug", message)
}

val VERSION_NAME = BuildConfig.VERSION_NAME
val VERSION_CODE = BuildConfig.VERSION_CODE.toString()
val DEVICE_INFO  = "${android.os.Build.MANUFACTURER} ${android.os.Build.PRODUCT} ${android.os.Build.MODEL}"

fun getFeedbackInfo() = "Version Code: $VERSION_CODE\n" +
            "Version Name: $VERSION_NAME\n" +
            "Device Info: $DEVICE_INFO\n\n"

fun Context.copyToClipboard(clipLabel: String, text: CharSequence){
    val clipboard = ContextCompat.getSystemService(this, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText(clipLabel, text))

    shortToast("Copied $clipLabel")
}

fun Fragment.collectStateFlow(body: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            body()
        }
    }
}

context(Fragment)
fun <T> StateFlow<T>.collectWithLifecycle(block: (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            this@collectWithLifecycle.collect {
                block(it)
            }
        }
    }
}