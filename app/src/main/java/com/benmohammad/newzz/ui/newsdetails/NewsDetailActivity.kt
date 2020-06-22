package com.benmohammad.newzz.ui.newsdetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.newzz.R
import com.benmohammad.newzz.data.model.CommentItem
import com.benmohammad.newzz.data.model.Item
import com.benmohammad.newzz.util.Errors
import com.benmohammad.newzz.util.translate
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_news_details.*
import kotlinx.android.synthetic.main.layout_comment.*
import kotlinx.android.synthetic.main.layout_comment_load.view.*
import kotlinx.coroutines.*
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

class NewsDetailActivity: AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var isSaved: Boolean = false

    private val commentsList = mutableListOf<CommentItem?>()
    private lateinit var commentsAdapter: CommentsAdapter

    private val newsDetailsViewModel by lazy {
        ViewModelProviders.of(this).get(NewsDetailsViewModel::class.java)
    }

    private val childCommentListener = object :
    CommentsAdapter.ChildCommentListener {
        var childCommentJob: Job? = null

        override fun onCollapse(rv: RecyclerView, loader: View) {
            childCommentJob?.cancel()
            rv.translate( rv.height * 1f , false)
            loader.isVisible = false
            loader.divideContainerLoad.removeAllViews()
        }

        override fun onExpand(
            commentItem: CommentItem,
            rv: RecyclerView,
            loader: View,
            depth: Int
        ) {
            childCommentJob = loadChildComments(commentItem, rv, loader, depth)
        }

    }

    private var retrySnack: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_details)

        retrySnack = Snackbar.make(root, "You are offline", Snackbar.LENGTH_INDEFINITE).setAction("Retry") { loadComments(true)}

        setCollapsingToolbarLayout()

        val item = getStoryItem()
        tvStoryTitle.text = item.title
        val kids = item.kids
        if(kids != null) {
            newsDetailsViewModel.commentIds = kids
            loadComments(false)
        } else {
            tvNoComments.isVisible = true
        }
    }

    private fun showMessageView(msg: String) {
        tvNoComments.apply{
            isVisible = true
            text = msg
        }
        commentsLoadContainer.isVisible = false
        rvComments.isVisible = false
        launch {
            delay(300)
            retrySnack?.setText(msg)?.show()
        }
    }

    private fun loadComments(isRefresh: Boolean) {
        launch{
            try {
                tvNoComments.isVisible = false
                val item = getStoryItem()
                tvStoryTitle.text = item.title

                newsDetailsViewModel.isLoading.observe(this@NewsDetailActivity, Observer{
                    if(it) {
                        loadMoreComments()
                    }
                })

            commentsLoadContainer.isVisible = true
            val list = newsDetailsViewModel.getCommentsAsync(isRefresh).await()
            commentsLoadContainer.isVisible = false
            commentsList.addAll(list)
            commentsAdapter = CommentsAdapter(commentsList, childCommentListener)
            rvComments.apply{
                isVisible = true
                adapter = commentsAdapter
                layoutManager = LinearLayoutManager(
                    this@NewsDetailActivity,
                    RecyclerView.VERTICAL, false
                )
            }
            storyContainer.setOnScrollChangeListener{ v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
                newsDetailsViewModel.scrollY = scrollY
                newsDetailsViewModel.scrollX = scrollX
                if(scrollY > oldScrollY) {
                    val lastItemY = rvComments.measuredHeight - v.measuredHeight
                    if(scrollY > lastItemY) {
                    if(newsDetailsViewModel.isNotFullLoaded() && newsDetailsViewModel.isLoading.value == false) {
                        newsDetailsViewModel.isLoading.value = true }
                    }
                }
                }

            storyContainer.post {
                storyContainer.scrollTo(newsDetailsViewModel.scrollX, newsDetailsViewModel.scrollY)
            }

        } catch (u: UnknownHostException) {
                showMessageView("You are Offline")

        } catch (ce: ConnectException) {
            showMessageView("You are Offline")
        } catch (se: SocketTimeoutException) {
            showMessageView("Timeout reached")
        } catch (e: Errors) {
                when(e) {
                    is Errors.OfflineException -> showMessageView("YOu are Offline")
                    is Errors.FetchException -> showMessageView(e.message ?: "Error occured")
                }
            }
        }
    }

    private fun loadMoreComments() {
        launch {
            try {
                addDummyLoadItem(true)
                val moreComments = newsDetailsViewModel.getMoreCommentsAsync().await()
                addDummyLoadItem(false)

                commentsList.apply {
                    clear()
                    addAll(moreComments)
                }
                commentsAdapter.notifyDataSetChanged()
                newsDetailsViewModel.isLoading.value = false
            } catch (u: UnknownHostException) {
                showMessageView("You are offline")
            } catch (ce: ConnectException) {
                showMessageView("You are Offline")
            } catch (se: SocketTimeoutException) {
                showMessageView("Timeout exception reached")
            } catch (e: Errors) {
                when(e) {
                    is Errors.OfflineException -> showMessageView("You are Offline")
                    is Errors.FetchException -> showMessageView(e.message ?: "Error occured")
                }
            }
        }
    }

    private fun addDummyLoadItem(toAdd: Boolean) {
        if(toAdd) {
            commentsList.add(null)
            commentsAdapter.notifyItemInserted(commentsList.size - 1)
        } else {
            commentsList.removeAt(commentsList.size - 1)
            commentsAdapter.notifyItemInserted(commentsList.size - 1)
        }
    }


    private fun loadChildComments(commentItem: CommentItem, rv: RecyclerView, loader: View, depth: Int): Job {
        return launch {

            try {


                val lf = LayoutInflater.from(this@NewsDetailActivity)
                loader.isVisible = true

                for (i in 0 until depth) {
                    val view = lf.inflate(R.layout.separator_view, commentContainer, false)
                    loader.divideContainerLoad.addView(view)
                }

                val childComments = newsDetailsViewModel.getChildCommentsAsync(commentItem).await()
                loader.isVisible = false

                val commentsAdapter = CommentsAdapter(
                    childComments, childCommentListener, depth
                )

                rv.apply {
                    rv.isVisible = true
                    adapter = commentsAdapter
                    layoutManager = LinearLayoutManager(
                        this@NewsDetailActivity,
                        RecyclerView.VERTICAL, false
                    )
                    translate(rv.height * 1f, true)
                }
            } catch (e: Exception) {
                when(e) {
                    is UnknownHostException,
                    is ConnectException,
                    is Errors.OfflineException,
                    is SocketTimeoutException -> {
                        loader.isVisible =false
                        Snackbar.make(root, "error, it seems you're not online", Snackbar.LENGTH_SHORT).show()
                    }
                    else -> throw e
                }
            }
        }
    }

    private fun setCollapsingToolbarLayout() {
        val logoUrl = intent.getStringExtra("LogoUrl")

        val item = getStoryItem()
        Picasso.get().load(logoUrl).placeholder(R.drawable.bn).into(ivLogoDetails)

        collapsingToolbar.title = if(item.domain != "nill") {
            item.domain.removeSuffix("/")
        } else {
            ""
        }

        tvAuthor.text = item.by
        tvScore.text = item.score.toString()

        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun getStoryItem() = intent.getSerializableExtra("itemObj") as Item


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.news_details_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_save -> {
                isSaved = !isSaved
                performSaveAction(isSaved, item)
            }
            R.id.action_web -> {
                val itemObj = getStoryItem()
                if(itemObj.url != null) {
                    openNewArticle(itemObj.url)
                } else {
                    Toast.makeText(this, "No News Article", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_share -> shareUrl()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let { setSaveAction(it.getItem(1)) }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun performSaveAction(isSaved: Boolean, item: MenuItem?) {
        val itemObj = getStoryItem()
        if(isSaved) {
            item?.icon =  resources.getDrawable(R.drawable.ic_bookmark)
            newsDetailsViewModel.insertItem(itemObj)
        } else {
            item?.icon = resources.getDrawable(R.drawable.ic_bookmark_border)
            newsDetailsViewModel.deleteItem(itemObj)
        }
    }

    private fun setSaveAction(item: MenuItem?) {
        launch {
            val itemObj = intent.getSerializableExtra("itemObj") as Item
            val itemIdOb = newsDetailsViewModel.getItemIdAsync(itemObj.id).await()
            isSaved = itemIdOb != null
            if(isSaved) {
                item?.icon = resources.getDrawable(R.drawable.ic_bookmark)
            } else {
                item?.icon = resources.getDrawable(R.drawable.ic_bookmark_border)
            }
        }
    }

    private fun openNewArticle(url: String) {
        val articleIntent = CustomTabsIntent.Builder().apply {
            setToolbarColor(ContextCompat.getColor(this@NewsDetailActivity, R.color.colorAccent))
            addDefaultShareMenuItem()
            setShowTitle(true)
        }.build()
        articleIntent.launchUrl(this, Uri.parse(url))
    }

    private fun shareUrl() {
        val sendIntent = Intent().apply{
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out the new HackerNews article ${getStoryItem().url}")
            type = "text/plain"
        }

        startActivity(Intent.createChooser(sendIntent, "Share News"))

    }}