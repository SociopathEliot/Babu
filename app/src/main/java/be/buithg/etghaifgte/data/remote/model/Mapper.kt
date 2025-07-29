// file: data/remote/model/EspnEvent.kt
package be.buithg.etghaifgte.data.remote.model

import be.buithg.etghaifgte.domain.model.Match


data class EspnEvent(
    val competitions: List<Competition>?,
    // в JSON у вас есть и это поле, берем его на всякий случай как fallback
    val venue: EventVenue?
)

data class Competition(
    val date: String?,
    val venue: Venue?,
    val competitors: List<Competitor>?,
    val status: StatusWrapper?,
    val type: CompetitionType?
)

data class Venue(
    val fullName: String?,
    val address: VenueAddress?
)

data class VenueAddress(
    val city: String?,
    val country: String?,
    val state: String?
)

data class EventVenue(
    val displayName: String?
)

data class Competitor(
    val homeAway: String?,
    val score: String?,
    val team: Team?
)

data class Team(
    val shortDisplayName: String?
)

data class StatusWrapper(
    val type: StatusType?
)

data class StatusType(
    val description: String?,
    val state: String?
)

data class CompetitionType(
    val text: String?
)

// Расширение для конвертации в ваш Match
fun EspnEvent.toMatch(league: String): Match? {
    // Первый competition
    val comp = competitions?.firstOrNull() ?: return null

    // Тип матча: из CompetitionType.text, если пуст — из названия лиги
    val matchType = comp.type?.text
        .takeUnless { it.isNullOrBlank() }
        ?: league

    // Город: сначала из competition.venue.address.city, иначе из event.venue.displayName
    val city = comp.venue
        ?.address
        ?.city
        .takeUnless { it.isNullOrBlank() }
        ?: this.venue?.displayName

    // Страна: из competition.venue.address.country
    val country = comp.venue
        ?.address
        ?.country

    // Домашняя и гостевая команды
    val home = comp.competitors?.find { it.homeAway == "home" }
    val away = comp.competitors?.find { it.homeAway == "away" }


    return Match(
        date        = comp.date?.substring(0, 10),
        dateTimeGMT = comp.date,
        status      = comp.status?.type?.description,
        matchType   = matchType,         // теперь не берём shortName
        league      = league,
        venue       = comp.venue?.fullName,
        city        = city,
        country      = country,
        teamA       = home?.team?.shortDisplayName,
        teamB       = away?.team?.shortDisplayName,
        scoreA      = home?.score?.toIntOrNull(),
        scoreB      = away?.score?.toIntOrNull(),
        matchEnded  = comp.status?.type?.state == "post"
    )
}
