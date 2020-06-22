package com.benmohammad.newzz.ui.home.banner

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.benmohammad.newzz.R
import com.benmohammad.newzz.data.model.Item
import com.benmohammad.newzz.ui.newsdetails.NewsDetailActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_banner_item.*

class TopBannerFragment: Fragment() {

    private val LOGO_URL = "https://logo.clearbit.com/"

    companion object {
        private val ITEM = "item"
        fun newInstance(item: Item): TopBannerFragment {
            return TopBannerFragment().apply {
                val bundle = Bundle().apply {
                    putSerializable(ITEM, item)
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
        return inflater.inflate(R.layout.fragment_banner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item = arguments?.get(ITEM) as Item
        tvTitleBanner.text = item.title
        tvAuthor.text = item.by
        tvComment.text = item.descendants.toString()
        tvScore.text = item.score.toString()
        Picasso.get().load("$LOGO_URL${item.domain}").placeholder(R.drawable.bn).into(ivLogoBanner)

        view.setOnClickListener {
            val options = ActivityOptions.makeSceneTransitionAnimation(
                requireActivity(),
                ivLogoBanner, "LogoTransition"
            )
            requireContext().startActivity(
                Intent(context, NewsDetailActivity::class.java).apply {
                    putExtra("LogoUrl", "$LOGO_URL${item.domain}")
                    putExtra("url", item.url)
                    putExtra("author", item.by)
                    putExtra("itemObj", item)
                }, options.toBundle()
            )
        }
    }
}