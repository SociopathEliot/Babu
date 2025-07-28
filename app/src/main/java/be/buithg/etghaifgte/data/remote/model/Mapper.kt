package be.buithg.etghaifgte.data.remote.model

import be.buithg.etghaifgte.domain.model.Match

fun EspnEvent.toMatch(league: String): Match? {
    val competition = competitions?.firstOrNull() ?: return null
    val teamA = competition.competitors?.find { it.homeAway == "home" }
    val teamB = competition.competitors?.find { it.homeAway == "away" }

    return Match(
        date = competition.date?.substring(0,10),
        dateTimeGMT = competition.date,
        status = competition.status?.type?.description,
        matchType = competition.type?.text,
        league = league,
        venue = competition.venue?.fullName,
        city = competition.venue?.address?.city,
        country = competition.venue?.address?.country,
        teamA = teamA?.team?.shortDisplayName,
        teamB = teamB?.team?.shortDisplayName,
        scoreA = teamA?.score?.toIntOrNull(),
        scoreB = teamB?.score?.toIntOrNull(),
        matchEnded = competition.status?.type?.state == "post"
    )
}
