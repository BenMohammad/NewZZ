package com.benmohammad.newzz.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.benmohammad.newzz.data.model.Item

@Database(entities = [Item::class], version = 1)
@TypeConverters(Converters::class)
abstract class ItemDatabase: RoomDatabase() {

    abstract fun itemsDao(): ItemsDao

    companion object {
        @Volatile
        private var instance: ItemDatabase? = null

        fun getDatabase(context: Context): ItemDatabase {
            if (instance != null) {
                return instance as ItemDatabase
            }
            synchronized(this) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    ItemDatabase::class.java,
                    "items_database"
                ).build()
                return instance as ItemDatabase

            }
        }
    }
}