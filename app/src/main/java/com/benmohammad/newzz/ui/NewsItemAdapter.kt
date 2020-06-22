package com.benmohammad.newzz.ui

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.benmohammad.newzz.R
import com.benmohammad.newzz.data.model.Item
import com.benmohammad.newzz.ui.newsdetails.NewsDetailActivity
import com.benmohammad.newzz.util.LOGO_URL
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_normal_item.view.*
import kotlinx.android.synthetic.main.layout_small_item.view.*

enum class ITEM_TYPE{
    SMALL, NORMAL
}

class NewsItemAdapter(
    private val newsItemList: List<Item?>,
    private val itemType: ITEM_TYPE,
    private val parentActivity: Activity
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val picasso = Picasso.get()
    private val VIEW_STORY = 0
    private val VIEW_LOAD = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if(viewType == VIEW_LOAD) {
            val view = inflater.inflate(R.layout.layout_normal_item_load, parent, false)
            LoadViewHolder(view)
        } else {
            val view = when (itemType) {
                ITEM_TYPE.SMALL -> inflater.inflate(R.layout.layout_small_item, parent, false)
                ITEM_TYPE.NORMAL -> inflater.inflate(R.layout.layout_normal_item, parent, false)
            }
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int = newsItemList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ViewHolder -> {
                val item = newsItemList[position]
                item?.let { holder.bind(it, itemType) }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = newsItemList[position]
        return if(item == null) VIEW_LOAD else VIEW_STORY
    }

    inner class LoadViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(item: Item, itemType: ITEM_TYPE) {
            with(itemView) {
                itemView.setOnClickListener {
                    val sharedIv = when(itemType) {
                        ITEM_TYPE.SMALL -> ivLogo
                        ITEM_TYPE.NORMAL -> ivLogoNews
                    }
                    val options = ActivityOptions.makeSceneTransitionAnimation(
                        parentActivity,
                        sharedIv, "LogoTransition"
                    )
                    context.startActivity(
                        Intent(
                            context,
                            NewsDetailActivity::class.java).apply {
                            putExtra("LogoUrl", "$LOGO_URL${item.domain}")
                            putExtra("url", item.url)
                            putExtra("author", item.by)
                            putExtra("itemObj", item)
                        },
                        options.toBundle()
                    )
                }
                when(itemType) {
                    ITEM_TYPE.SMALL -> {
                        tvTitle.text = item.title
                        picasso.load("$LOGO_URL${item.domain}")
                            .placeholder(R.drawable.bn)
                            .fit()
                            .centerInside()
                            .into(ivLogo)
                    }
                    ITEM_TYPE.NORMAL -> {
                        tvTitleNormal.text = item.title
                        tvAuthorNormal.text = item.by
                        tvCommentNormal.text = item.descendants.toString()
                        tvScoreNormal.text = item.score.toString()
                        picasso.load("$LOGO_URL${item.domain}").placeholder(R.drawable.bn).into(ivLogoNews)
                    }

                }            }
        }
    }
}