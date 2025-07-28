package be.buithg.etghaifgte.domain.model

import java.io.Serializable

data class Match(
    val date: String?,
    val dateTimeGMT: String?,
    val status: String?,
    val matchType: String?,
    val league: String?,
    val venue: String?,
    val city: String?,
    val country: String?,
    val teamA: String?,
    val teamB: String?,
    val scoreA: Int?,
    val scoreB: Int?,
    val matchEnded: Boolean
) : Serializable
