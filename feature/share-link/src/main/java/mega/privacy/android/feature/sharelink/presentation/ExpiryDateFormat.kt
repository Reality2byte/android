package mega.privacy.android.feature.sharelink.presentation

import java.text.DateFormat
import java.util.Date
import java.util.TimeZone

/** Link expiry dates are stored and selected in UTC, so they are formatted in UTC too. */
internal val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/**
 * Formats a link expiry date, given in UTC milliseconds, for display.
 */
internal fun formatExpiryDate(millis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM)
        .apply { timeZone = UTC }
        .format(Date(millis))
