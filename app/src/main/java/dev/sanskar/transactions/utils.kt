package dev.sanskar.transactions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
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

const val ISSUE_URL = "https://github.com/sanskar10100/Transactions/issues/new"

fun Long.asFormattedDateTime() : String {
    return SimpleDateFormat(
        "dd/MM/yy hh:mm aaa",
        Locale.ENGLISH
    ).format(Date(this))
}

val TextInputLayout.text: String
get() {
    return this.editText?.text.toString() ?: ""
}

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