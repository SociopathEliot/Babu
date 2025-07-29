package be.buithg.etghaifgte.utils

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

fun String?.parseUtcToLocal(): LocalDateTime? {
    if (this == null) return null
    return runCatching { OffsetDateTime.parse(this) }
        .getOrNull()
        ?.atZoneSameInstant(ZoneId.systemDefault())

        ?.toLocalDateTime()
}
