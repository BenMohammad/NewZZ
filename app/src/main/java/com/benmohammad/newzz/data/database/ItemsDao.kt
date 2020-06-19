package com.benmohammad.newzz.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.benmohammad.newzz.data.model.Item

@Dao
interface ItemsDao {

    @Query("SELECT * FROM items")
    fun getSavedStories(): LiveData<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(item: Item)

    @Delete
    suspend fun deleteStory(item: Item)

    @Query("SELECT id FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemId(id: Long): Long?
}