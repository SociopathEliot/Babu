package be.buithg.etghaifgte.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import be.buithg.etghaifgte.data.local.dao.PredictionDao
import be.buithg.etghaifgte.data.local.dao.NoteDao
import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.data.local.entity.NoteEntity

@Database(
    entities = [PredictionEntity::class, NoteEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun predictionDao(): PredictionDao
    abstract fun noteDao(): NoteDao
}
