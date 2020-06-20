package com.benmohammad.newzz.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.benmohammad.newzz.data.model.Item
import com.benmohammad.newzz.data.remote.HNApiClient
import com.benmohammad.newzz.util.Errors
import com.benmohammad.newzz.util.getSafeResponse
import com.benmohammad.newzz.util.isConnected
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class DashNewsViewModel(application: Application): AndroidViewModel(application) {

    private val client = HNApiClient(application).hnApiService

    private val topItems = mutableListOf<Item>()
    private val newItems = mutableListOf<Item>()
    private val showItems = mutableListOf<Item>()
    private val jobItems = mutableListOf<Item>()
    private val askItems = mutableListOf<Item>()

    var bannerIdx = 0

    fun getItemAsync(type: String, isRefresh: Boolean): Deferred<List<Item>> {
        return viewModelScope.async(Dispatchers.IO) {
                val list = getListCategory(type)
                if(list.isNotEmpty() && !isRefresh) {
                    return@async list
                } else {
                    if(isConnected(getApplication())) {
                        val itemsIds = getItemIds(type)
                        val itemList = getItemList(itemsIds, type == "Top")
                        list.apply {
                            clear()
                            addAll(itemList)
                        }
                        list
                    }else {
                        throw Errors.OfflineException()
                    }
                }
        }
    }

    private fun getListCategory(type: String) = when(type) {
        "Top" -> topItems
        "New" -> newItems
        "Show" -> showItems
        "Job" -> jobItems
        "Ask" -> askItems
        else -> throw Errors.UnknownCategory()
    }

    private suspend fun getItemIds(type: String) = when(type) {
        "Top" -> getSafeResponse(client.topStories())
        "New" -> getSafeResponse(client.newStories())
        "Show" -> getSafeResponse(client.showStories())
        "Job" -> getSafeResponse(client.jobStories())
        "Ask" -> getSafeResponse(client.askStories())
        else -> throw Errors.UnknownCategory()

    }
    private suspend fun getItemList(list: List<Long>, isBanner: Boolean = false): List<Item> {
        return if(list.isNotEmpty()) {
            val end = if(isBanner) 6 else 4
            val itemList = mutableListOf<Item>()
            for(i in 0 until end) {
                val item = client.getItem(list[i]).body()
                item?.let { itemList.add(item) }
            }
            itemList
        } else {
            emptyList()
        }

    }
}