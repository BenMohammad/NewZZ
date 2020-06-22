package com.benmohammad.newzz.ui.newstype

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.newzz.R
import com.benmohammad.newzz.data.model.Item
import com.benmohammad.newzz.ui.ITEM_TYPE
import com.benmohammad.newzz.ui.NewsItemAdapter
import com.benmohammad.newzz.util.Errors
import com.benmohammad.newzz.util.isConnected
import com.benmohammad.newzz.util.isPortrait
import com.benmohammad.newzz.util.setTopDrawable
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_news_type.*
import kotlinx.android.synthetic.main.layout_error.*
import kotlinx.android.synthetic.main.layout_multi_normal_items_shimmer.*
import kotlinx.coroutines.*
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

class NewsTypeFragment: Fragment(), CoroutineScope {

    val supervisor = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + supervisor

    private val newsTypeViewModel by lazy {
        ViewModelProviders.of(this@NewsTypeFragment)
            .get(NewsTypeViewModel::class.java)
    }

    private var retrySnack: Snackbar? = null
    private val mutableListItem = mutableListOf<Item?>()

    companion object{
        private val NEWSTYPE: String = "news_type"
        fun newInstance(newsType: String): NewsTypeFragment {
            return NewsTypeFragment().apply {
                val bundle = Bundle().apply {
                    putString(NEWSTYPE, newsType)
                }
                arguments = bundle
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news_type, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launch {

            retrySnack = Snackbar.make(newsTypeContainer, "You are offline", Snackbar.LENGTH_SHORT)
                .setAction("Retry") {
                    Log.i("PUI", "retry action")
                    retryAction()
                }
            val newsType = arguments?.get(NEWSTYPE) as String
            if(newsType == "Saved") {
                showSavedItems()
                newsTypeContainer.isEnabled = false
            } else {
                newsTypeViewModel.isLoading.observe(viewLifecycleOwner, Observer {
                    if(it) {
                        if(isConnected(requireContext())) {
                            loadMoreStories(mutableListItem)
                        } else {
                            newsTypeViewModel.isLoading.value = false
                            retrySnack?.setText("You are offline")?.show()

                        }
                    }
                })
                loadStories(newsType, false)
                newsTypeContainer.apply {
                    setProgressViewOffset(true, 100, 250)
                    setOnRefreshListener { loadStories(newsType, true) }
                }
            }
        }
    }

    private fun showNoSavedItemsView() {
        tvError.apply {
            isVisible = true
            text = "No Saved items"
            val drawable = resources.getDrawable(R.drawable.ic_empty_bin)
            setTopDrawable(drawable)
        }
    }

    private fun showSavedItems() {
        newsTypeViewModel.savedStories.observe(viewLifecycleOwner, Observer{
            if(it.isEmpty())
                showNoSavedItemsView()

                mutableListItem.apply {
                    clear()
                    addAll(it)
                }
                rvItems.adapter?.notifyDataSetChanged()

        })
            shimmerNewsType.apply {
                stopShimmer()
                isVisible = false
            }
            setupRecyclerView(mutableListItem)
    }

    private fun retryAction() {
        if(isConnected(requireContext())) {
            val newsType = arguments?.get(NEWSTYPE) as String
            loadStories(newsType, true)
        } else {
            launch {
                delay(300)
                retrySnack?.setText("You are offline")?.show()
            }
        }
    }

    private fun loadStories(newsType: String, refresh: Boolean) {
        launch {
            try {
                showStoryItems()
                setLoadView(true)
                val itemList = newsTypeViewModel.getStoriesAsync(newsType, refresh).await()
                mutableListItem.apply {
                    clear()
                    addAll(itemList)
                }

                setLoadView(false)

                rvItems.adapter?.notifyDataSetChanged() ?: setupRecyclerView(mutableListItem)
                newsTypeContainer.isRefreshing = false
            }catch (u: UnknownError) {
                showOfflineView()
            }catch (ce: ConnectException) {
                showOfflineView()
            }catch (se: SocketTimeoutException) {
                showErrorView()
            } catch(e: Errors) {
                when(e) {
                    is Errors.OfflineException -> showOfflineView()
                    is Errors.FetchException -> showErrorView()
                }
            } finally {
                shimmerNewsType?.apply {
                    stopShimmer()
                    isVisible = false
                }
            }
        }
    }

    private fun showErrorView() {
        rvItems.isVisible = false
        tvError.apply {
            isVisible = true
            text = "Error Occurred"
            val drawable = resources.getDrawable(R.drawable.ic_no_wifi)
            setTopDrawable(drawable)
        }
        newsTypeContainer.isRefreshing = false
        retrySnack?.setText("You are Offline")?.show()
    }

    private fun showOfflineView() {

        rvItems.isVisible = false
        tvError.apply{
            isVisible = true
            text = resources.getString(R.string.no_internet)
            val drawable = resources.getDrawable(R.drawable.ic_no_wifi)
            setTopDrawable(drawable)
        }
        newsTypeContainer.isRefreshing = false
        retrySnack?.setText("You are Offline")?.show()


    }

    private fun showStoryItems() {
        tvError.isVisible = false
        rvItems.isVisible = true
        retrySnack?.dismiss()
    }


    private fun setLoadView(isLoading: Boolean) {
        shimmerNewsType.apply {
            if(isLoading) startShimmer() else stopShimmer()

            isVisible = isLoading
        }
        rvItems.isVisible = !isLoading
    }

    private fun setupRecyclerView(mutableItemList: MutableList<Item?>) {
        rvItems.apply {
            isVisible = true
            adapter = NewsItemAdapter(
                mutableItemList,
                ITEM_TYPE.NORMAL,
                requireActivity()
            )
            layoutManager =
                if(isPortrait(requireContext())) {
                   LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false).apply {
                       scrollToPosition(newsTypeViewModel.scrollPosition)
                   }
                } else {
                    GridLayoutManager(requireContext(), 2).apply {
                        scrollToPosition(newsTypeViewModel.scrollPosition)
                    }
                }

            if(!isPortrait(requireContext())) {
                addItemDecoration(SpaceItemDecoration(10))
            }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = rvItems.layoutManager as LinearLayoutManager

                    newsTypeViewModel.scrollPosition = layoutManager.findFirstCompletelyVisibleItemPosition()

                    if(layoutManager.findLastCompletelyVisibleItemPosition() == mutableItemList.size - 1) {
                        if(newsTypeViewModel.isNotFullLoaded() && newsTypeViewModel.isLoading.value == false) {
                            if(isConnected(requireContext())) {
                                retrySnack?.dismiss()
                                newsTypeViewModel.isLoading.value = true
                            } else {
                                retrySnack?.setText("You are offline")
                            }
                        }
                    }
                }
            })
        }
    }

    private fun loadMoreStories(mutableItemsList: MutableList<Item?>) {
        launch {
            try {
                addDummyLoadItem(true)
                val newsItemList = newsTypeViewModel.loadMoreItemsAsync().await()
                addDummyLoadItem(false)

                if(newsItemList.isNotEmpty()) {
                    mutableItemsList.addAll(newsItemList)
                    rvItems.adapter?.notifyDataSetChanged()
                }
            } catch (u: UnknownHostException) {
                addDummyLoadItem(false)
                retrySnack?.setText("You are offline")?.show()
            } catch (ce: ConnectException) {
                addDummyLoadItem(false)
                retrySnack?.setText("You are offline")?.show()
            } catch(se: SocketTimeoutException) {
                addDummyLoadItem(false)
                retrySnack?.setText("Error occurred")
            } finally {
                newsTypeViewModel.isLoading.value = false
            }
        }
    }

    private fun addDummyLoadItem(toAdd: Boolean) {
        if(toAdd) {
            mutableListItem.add(null)
            rvItems.adapter?.notifyItemInserted(mutableListItem.size - 1)
        } else {
            mutableListItem.removeAt(mutableListItem.size - 1)
            rvItems.adapter?.notifyItemRemoved(mutableListItem.size - 1)
        }
    }

    override fun onDetach() {
        coroutineContext.cancelChildren()
        retrySnack?.dismiss()
        super.onDetach()
    }



    inner class SpaceItemDecoration(private val space: Int): RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.left = space
            outRect.right = space
            outRect.bottom = space

            if(parent.getChildLayoutPosition(view) == 0) {
                outRect.top = space
            } else {
                outRect.top = 0
            }
        }
    }

}