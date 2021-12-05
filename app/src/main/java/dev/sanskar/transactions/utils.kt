package dev.sanskar.transactions

import java.text.SimpleDateFormat
import java.util.*

fun Long.asFormattedDateTime() : String {
    return SimpleDateFormat(
        "dd/MM/yy hh:mm aaa",
        Locale.ENGLISH
    ).format(Date(this))
}