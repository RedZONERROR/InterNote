package com.inter.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.inter.data.local.entities.Folder
import com.inter.data.local.entities.Note

@Database(entities = [Note::class, Folder::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inter_notes_standard.db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeAndReset(context: Context) {
            synchronized(this) {
                try {
                    INSTANCE?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                INSTANCE = null
                try {
                    context.deleteDatabase("inter_notes_secure.db")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    context.deleteDatabase("inter_notes_standard.db")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
