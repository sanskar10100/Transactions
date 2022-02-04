package dev.sanskar.transactions

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

const val VIEW_OPTIONS_REQUEST_KEY = "view_options_request_key"
const val KEY_SELECTED_VIEW_OPTION = "selected"

fun Long.asFormattedDateTime() : String {
    return SimpleDateFormat(
        "dd/MM/yy hh:mm aaa",
        Locale.ENGLISH
    ).format(Date(this))
}

fun TextInputLayout.getText(): String {
    return this.editText?.text?.toString() ?: ""
}

fun View.shortSnackbar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}

fun Context.shortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}