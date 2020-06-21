package com.benmohammad.newzz.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import com.benmohammad.newzz.R
import com.benmohammad.newzz.ui.home.HomeFragment
import com.benmohammad.newzz.ui.newstype.NewsTypeFragment
import com.benmohammad.newzz.util.getSharedPrefs
import com.benmohammad.newzz.util.isFirstRun
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_news_details.*
import kotlinx.android.synthetic.main.layout_main_content.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private val supervisor = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + supervisor

    private var fragTag = "Home"
    private var toolbarTitle = "Hacker News"

    private val topAlertWorking = "top alerts"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setNavDrawer()
        setFragment(savedInstanceState)

        if(isFirstRun()) {
            handOffTopAlertTask(4)
            getSharedPrefs().edit().putBoolean("First", false).apply()
        }

        ivNotif.setOnClickListener { showNotifPeriodDialog() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putString("FRAG_TAG", fragTag)
            putString("TOOL_TITLE", toolbarTitle)
        }
        val frag = supportFragmentManager.findFragmentByTag(fragTag) as Fragment
        supportFragmentManager.putFragment(outState, fragTag, frag)
    }

    private fun setFragment(bundle: Bundle?) {
        if(bundle != null) {
            fragTag = bundle["FRAG_TAG" ] as String
            toolbarTitle = bundle["TOOL_TITLE"] as String
            setToolBar(toolbarTitle)
            val frag = supportFragmentManager.getFragment(bundle, fragTag) as Fragment
            supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment(), "Home")
                .commit()
        }
    }

    private fun setFragmentMenuItem(menuItem: MenuItem, fragment: Fragment, tag: String?) : Boolean {
        if(supportFragmentManager.findFragmentByTag(tag) == null) {
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment, tag).commit()
        }
        menuItem.isChecked = true
        drawerLayout.closeDrawer(Gravity.LEFT)
        return true
    }

    private fun setNavDrawer() {
        setSupportActionBar(matToolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val drawerToggle = ActionBarDrawerToggle(this, drawerLayout, matToolBar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        nav_view.setNavigationItemSelectedListener {
            return@setNavigationItemSelectedListener when(it.itemId) {
                R.id.action_dash -> {
                    fragTag = "Home"
                    toolbarTitle = "Hacker News"
                    setToolBar(toolbarTitle)
                    setFragmentMenuItem(it, HomeFragment(), "Home")
                }
                R.id.action_ask -> {
                    fragTag = "Ask"
                    toolbarTitle = "Ask"
                    setToolBar(toolbarTitle)
                    setFragmentMenuItem(it, NewsTypeFragment.newInstance("Ask"), "Ask")
                }

                R.id.action_Show -> {
                    fragTag = "Show"
                    toolbarTitle = "Show"
                    setToolBar(toolbarTitle)
                    setFragmentMenuItem(it, NewsTypeFragment.newInstance("Show"), "Show")
                }
                R.id.action_job -> {
                    fragTag = "Job"
                    toolbarTitle = "Job"
                    setToolBar(toolbarTitle)
                    setFragmentMenuItem(it, NewsTypeFragment.newInstance("Job"), "Job")
                }

                R.id.action_new -> {
                    fragTag = "New"
                    toolbarTitle = "New"
                    setToolBar(toolbarTitle)
                    setFragmentMenuItem(it, NewsTypeFragment.newInstance("Mew"), "New")
                }
                R.id.action_top -> {
                    fragTag = "Top"
                    toolbarTitle = "Top"
                    setToolBar(toolbarTitle)
                    setFragmentMenuItem(it, NewsTypeFragment.newInstance("Top"), "Top")
                }

                R.id.action_saved -> {
                    fragTag = "Saved"
                    toolbarTitle = "Saved"
                    setToolBar(toolbarTitle)
                    setFragmentMenuItem(it, NewsTypeFragment.newInstance("Saved"), "Saved")
                }
                else -> false
            }
        }
        setToolBar(toolbarTitle)
        nav_view.setCheckedItem(R.id.action_dash)
    }

    private fun setToolBar(type: String) {
        tvToolbarTitle.text = type
        when(type) {
            "Hacker News" -> {
                tvToolbarTitle.setTextColor(resources.getColor(R.color.colorAccent))
                ivNotif.visibility = View.VISIBLE
            }
            "Ask" -> {
                tvToolbarTitle.setTextColor(resources.getColor(R.color.colorJamun))
                ivNotif.visibility = View.INVISIBLE
            }
            "Show" -> {
                tvToolbarTitle.setTextColor(resources.getColor(R.color.colorRed))
                ivNotif.visibility = View.INVISIBLE
            }
            "Job" -> {
                tvToolbarTitle.setTextColor(resources.getColor(R.color.colorGreen))
                ivNotif.visibility = View.INVISIBLE
            }

            "New" -> {
                ivNotif.visibility = View.INVISIBLE
                tvToolbarTitle.setTextColor(resources.getColor(R.color.colorAccent))
            }

            "Top" -> {
                tvToolbarTitle.setTextColor(resources.getColor(R.color.colorOrange))
                ivNotif.visibility = View.INVISIBLE
            }

            "Saved" -> {
                tvToolbarTitle.setTextColor(resources.getColor(R.color.colorJamun))
                ivNotif.visibility = View.INVISIBLE
            }
        }
    }

    private fun showNotifPeriodDialog() {

    }

    private fun handOffTopAlertTask(hour: Long){}

    private fun turnOffTopAlerts(){}
}
