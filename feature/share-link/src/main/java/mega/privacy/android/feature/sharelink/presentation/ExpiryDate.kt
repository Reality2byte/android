package mega.privacy.android.feature.sharelink.presentation

import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * A link expires at the end of the day the user picked, in their own timezone — matching the legacy
 * Get link screen, which sets the expiry to 23:59 local time.
 *
 * The date picker works in a different space: it returns, and pre-selects from, midnight UTC of the
 * chosen day. [endOfLocalDay] and [utcMidnightOfLocalDay] convert between the two so that the value
 * held in the UI state and sent to the SDK is always a true instant.
 */
internal val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/** Formats a link expiry [millis] instant as a date in the user's timezone. */
internal fun formatExpiryDate(millis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(millis))

/**
 * Converts a date picker result — midnight UTC of the chosen day — to the instant at which that day
 * ends locally.
 */
internal fun endOfLocalDay(utcMidnightMillis: Long): Long {
    val picked = Calendar.getInstance(UTC).apply { timeInMillis = utcMidnightMillis }
    return Calendar.getInstance().apply {
        clear()
        set(
            picked.get(Calendar.YEAR),
            picked.get(Calendar.MONTH),
            picked.get(Calendar.DAY_OF_MONTH),
            23,
            59,
            59,
        )
    }.timeInMillis
}

/**
 * Converts a stored expiry instant back to midnight UTC of the local day it falls on, so the date
 * picker pre-selects the day the user actually chose.
 */
internal fun utcMidnightOfLocalDay(millis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = millis }
    return Calendar.getInstance(UTC).apply {
        clear()
        set(
            local.get(Calendar.YEAR),
            local.get(Calendar.MONTH),
            local.get(Calendar.DAY_OF_MONTH),
            0,
            0,
            0,
        )
    }.timeInMillis
}

/** Today in the user's timezone, in the date picker's midnight-UTC space. */
internal fun todayStartUtcMillis(): Long = utcMidnightOfLocalDay(System.currentTimeMillis())
