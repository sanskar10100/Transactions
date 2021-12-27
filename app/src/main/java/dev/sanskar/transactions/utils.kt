package dev.sanskar.transactions

import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

fun Long.asFormattedDateTime() : String {
    return SimpleDateFormat(
        "dd/MM/yy hh:mm aaa",
        Locale.ENGLISH
    ).format(Date(this))
}

fun TextInputLayout.getText(): String {
    return this.editText?.text?.toString() ?: ""
}