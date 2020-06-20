package com.benmohammad.newzz.ui.newsdetails

import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.newzz.R
import com.benmohammad.newzz.data.model.CommentItem
import com.benmohammad.newzz.util.getDateTime
import com.benmohammad.newzz.util.invert
import kotlinx.android.synthetic.main.layout_comment.view.*
import kotlinx.android.synthetic.main.layout_comment_load.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CommentsAdapter(
    private val comments: List<CommentItem>,
    private val childCommentListener: ChildCommentListener,
    private val depth: Int = 0
): RecyclerView.Adapter<RecyclerView.ViewHolder>(), CoroutineScope {


    private val VIEW_LOAD = 0
    private val VIEW_COMMENT = 1



    interface ChildCommentListener{
        fun onExpand(commentItem: CommentItem, rv: RecyclerView, loader: View, depth: Int)
        fun onCollapse(rv: RecyclerView, loader: View)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == VIEW_LOAD) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_comment_load, parent, false)
            LoadViewHolder(view)
        }else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_comment, parent, false)
            CommentHolder(view)
        }
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is CommentHolder -> {
                val item = comments[position]
                item?.let {
                    holder.bind(it)
                }
            }
            is LoadViewHolder -> holder.bind()

        }    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    inner class LoadViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind() {
            with(itemView) {
                val lf = LayoutInflater.from(context)
                for(i in 0 until depth) {
                    val view = lf.inflate(R.layout.separator_view, commentLoadContainer, false)
                    divideContainerLoad.addView(view)

                }
            }
        }
    }

    inner class CommentHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(commentItem: CommentItem) {
            with(itemView) {
                tvAuthorComment.text = commentItem.item.by

                tvCommentStory.apply {
                    text = Html.fromHtml(commentItem.item.text)
                    movementMethod = LinkMovementMethod.getInstance()
                }
                tvTimeComment.text = getDateTime(commentItem.item.time)

                val lf = LayoutInflater.from(context)
                for(I in 0 until depth) {
                    val view = lf.inflate(R.layout.separator_view, commentContainer, false)
                    divideContainer.addView(view)
                }

                val kids = commentItem.item.kids
                if(kids != null && kids.isNotEmpty()) {
                    tvChildren.text = "${kids.size} ${if (kids.size == 1) "comment" else "comments"}"
                    if(commentItem.isExpanded) {
                        ivExpand.invert()
                        childCommentListener.onExpand(commentItem, rvChildren, commentLoader, depth + 1)
                    }

                    subCommentContainer.setOnClickListener{
                        if(commentItem.isExpanded) {
                            subCommentContainer.preventRapidBtnClick()
                            ivExpand.invert()
                            childCommentListener.onCollapse(rvChildren, commentLoader)
                        } else {
                            subCommentContainer.preventRapidBtnClick()
                            ivExpand.invert()
                            childCommentListener.onExpand(commentItem, rvChildren, commentLoader, depth + 1)
                        }
                        commentItem.isExpanded = false
                    }
                } else {
                    tvChildren.isVisible = false
                    ivExpand.isVisible = false
                }
            }
        }

        private fun View.preventRapidBtnClick(){
            this.isClickable = false
            launch {
                delay(300)
                this@preventRapidBtnClick.isClickable = true
            }
        }


    }}