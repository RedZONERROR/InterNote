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
                try {
                    val instance = buildDatabase(context, true)
                    // Try to force-open database file to verify passphrase & integrity immediately
                    instance.openHelper.writableDatabase
                    INSTANCE = instance
                    instance
                } catch (t: Throwable) {
                    t.printStackTrace()
                    // Decryption failed, database corrupted, or native loading failed (like UnsatisfiedLinkError)
                    try {
                        context.deleteDatabase("inter_notes_secure.db")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        val instance = buildDatabase(context, false)
                        instance.openHelper.writableDatabase
                        INSTANCE = instance
                        instance
                    } catch (inner: Throwable) {
                        inner.printStackTrace()
                        // Final absolute fallback: fresh unencrypted build
                        val instance = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "inter_notes_standard.db"
                        )
                        .fallbackToDestructiveMigration()
                        .build()
                        INSTANCE = instance
                        instance
                    }
                }
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
                context.deleteDatabase("inter_notes_secure.db")
                context.deleteDatabase("inter_notes_standard.db")
            }
        }

        private fun buildDatabase(context: Context, encrypt: Boolean): AppDatabase {
            if (encrypt) {
                try {
                    // Retrieve or generate secure database passphrase
                    val prefs = context.getSharedPreferences("secure_crypt_prefs", Context.MODE_PRIVATE)
                    var passkey = prefs.getString("db_crypto_passphrase", null)
                    if (passkey == null) {
                        passkey = java.util.UUID.randomUUID().toString()
                        prefs.edit().putString("db_crypto_passphrase", passkey).apply()
                    }

                    net.sqlcipher.database.SQLiteDatabase.loadLibs(context)

                    val factory = SupportFactory(passkey.toByteArray())

                    return Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "inter_notes_secure.db"
                    )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                } catch (t: Throwable) {
                    t.printStackTrace()
                    // Fallthrough to standard building if any exception/error occurs
                }
            }

            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "inter_notes_standard.db"
            )
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}
