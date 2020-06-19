package com.benmohammad.newzz.data

import androidx.annotation.WorkerThread
import com.benmohammad.newzz.data.database.ItemsDao
import com.benmohammad.newzz.data.model.Item

class ItemsRepository(private val itemsDao: ItemsDao) {

    val savedStories = itemsDao.getSavedStories()

    @WorkerThread
    suspend fun insertStory(item: Item) {
        itemsDao.insertStory(item)
    }

    @WorkerThread
    suspend fun deleteStory(item: Item) {
        itemsDao.deleteStory(item)
    }

    @WorkerThread
    suspend fun getItemId(id: Long): Long? = itemsDao.getItemId(id)
}