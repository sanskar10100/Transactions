package dev.sanskar.transactions

import android.content.Context
import android.view.View
import android.widget.Toast
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