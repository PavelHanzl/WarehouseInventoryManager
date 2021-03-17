package cz.pavelhanzl.warehouseinventorymanager.repository

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun getDateTime(): String {
    return DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withLocale(Locale.getDefault())
        .withZone(
            ZoneId.systemDefault()
        ).format(Instant.now())
}
