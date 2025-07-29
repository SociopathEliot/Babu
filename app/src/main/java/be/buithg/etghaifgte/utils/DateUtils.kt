package be.buithg.etghaifgte.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun String?.parseUtcToLocal(): LocalDateTime? {
    if (this == null) return null
    return runCatching { Instant.parse(this) }
        .getOrNull()
        ?.atZone(ZoneId.systemDefault())
        ?.toLocalDateTime()
}
