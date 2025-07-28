package be.buithg.etghaifgte.data.remote.model

data class ScoreboardResponse(
    val events: List<EspnEvent>?
)

data class EspnEvent(
    val date: String?,
    val competitions: List<Competition>?,
    val status: StatusWrapper?
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
    val country: String?
)

data class Competitor(
    val homeAway: String?,
    val score: String?,
    val team: Team?
)

data class Team(
    val name: String?,
    val shortDisplayName: String?,
    val abbreviation: String?
)

data class StatusWrapper(
    val type: StatusType?
)

data class StatusType(
    val description: String?,
    val state: String?,
    val shortDetail: String?
)

data class CompetitionType(
    val text: String?
)
