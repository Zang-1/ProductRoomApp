package edu.vlu.week07.productroom.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProductEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        // @Volatile: a write to INSTANCE is visible to every thread at once
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Double-Check Locking: check without the lock (fast path), then check again inside it
        // so two threads arriving together cannot build two databases.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vlu_product_catalog_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
