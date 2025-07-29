package be.buithg.etghaifgte.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teamA: String,
    val teamB: String,
    val dateTime: String,
    val matchType: String,
    val stadium: String,
    val city: String,
    val country: String,
    val pick: String,
    val predicted: Int,
    val corrects: Int,
    val upcoming: Int,
    val wonMatches: Int
)
