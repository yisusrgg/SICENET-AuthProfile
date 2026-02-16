package com.example.sicenet_authprofile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sicenet_authprofile.data.local.dao.SicenetDao
import com.example.sicenet_authprofile.data.local.entities.CargaAcademicaEntity

@Database(
    entities = [CargaAcademicaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SicenetDatabase : RoomDatabase() {
    abstract fun sicenetDao(): SicenetDao

    companion object {
        @Volatile
        private var Instance: SicenetDatabase? = null

        fun getDatabase(context: Context): SicenetDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, SicenetDatabase::class.java, "sicenet_database")
                    .fallbackToDestructiveMigration() // Si cambias la DB, borra la vieja y crea nueva
                    //.addMigrations(MIGRATION_1_2)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

//val MIGRATION_1_2 = object : Migration(1, 2) { // De la versión 1 a la versión 2
//    override fun migrate(db: SupportSQLiteDatabase) {
//        db.execSQL(
//            "ALTER TABLE recordatorios ADD COLUMN opcion INTEGER NOT NULL DEFAULT 0"
//        )
//    }
//}