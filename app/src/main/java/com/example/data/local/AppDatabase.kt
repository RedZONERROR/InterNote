package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.entities.Folder
import com.example.data.local.entities.Note
import net.sqlcipher.database.SupportFactory

@Database(entities = [Note::class, Folder::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            // Retrieve or generate secure database passphrase
            val prefs = context.getSharedPreferences("secure_crypt_prefs", Context.MODE_PRIVATE)
            var passkey = prefs.getString("db_crypto_passphrase", null)
            if (passkey == null) {
                passkey = java.util.UUID.randomUUID().toString()
                prefs.edit().putString("db_crypto_passphrase", passkey).apply()
            }

            val factory = SupportFactory(passkey.toByteArray())

            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "inter_notes_secure.db"
            )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}
