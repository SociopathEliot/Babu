// file: data/remote/model/EspnEvent.kt
package be.buithg.etghaifgte.data.remote.model

import be.buithg.etghaifgte.domain.model.Match

fun EspnEvent.toMatch(league: String): Match? {
    val competition = competitions?.firstOrNull() ?: return null
    val teamA = competition.competitors?.find { it.homeAway == "home" }
    val teamB = competition.competitors?.find { it.homeAway == "away" }

    val country = competition.venue?.address?.country
        ?: competition.venue?.address?.state?.let { "USA" }
        ?: when (league) {
            "nfl", "nba", "mlb", "nhl" -> "USA"
            "eng.1" -> "England"
            "fra.1" -> "France"
            else -> null
        }

    val matchType = competition.type?.text

    return Match(
        date         = competition.date?.substring(0,10),
        dateTimeGMT  = competition.date,
        status       = competition.status?.type?.description,
        matchType    = matchType,
        league       = league,
        venue        = competition.venue?.fullName,
        city         = competition.venue?.address?.city,
        country      = country,
        teamA        = teamA?.team?.shortDisplayName,
        teamB        = teamB?.team?.shortDisplayName,
        scoreA       = teamA?.score?.toIntOrNull(),
        scoreB       = teamB?.score?.toIntOrNull(),
        matchEnded   = competition.status?.type?.state == "post"
    )
}

data class EspnEvent(
    val competitions: List<Competition>?,
    val status: StatusWrapper?,
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


