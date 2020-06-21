package com.benmohammad.newzz.util

import android.animation.Animator
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import androidx.core.view.isVisible
import retrofit2.Response
import java.time.Month
import java.util.*


val LOGO_URL = "https://logo.clearbit.com/"

fun getDateTime(timeInMilli: Long): String {
    val currTimeInMilli = System.currentTimeMillis() / 1000
    val diff = currTimeInMilli - timeInMilli
    val hoursPast = diff / (60 * 60)
    if (hoursPast < 24) {
        return if (hoursPast == 0L) {
            val minPast = diff / 60
            "$minPast min"
        } else {
            "$hoursPast h"
        }
    }

    val cal = Calendar.getInstance().apply {
        timeInMillis = timeInMilli * 1000
    }

    return with(cal) {
        val day = get(Calendar.DAY_OF_MONTH) + 1
        val mm = get(Calendar.MONTH)
        val year = get(Calendar.YEAR)
        "$day-$mm-$year"

    }
}
fun <T>getSafeResponse(response: Response<List<T>>): List<T> {
    return if(response.isSuccessful) {
        response.body() ?: emptyList()
    } else {
        throw Errors.FetchException(response.errorBody()?.string() ?: "Unknown Error")
    }
}

fun isPortrait(context: Context): Boolean {
    return context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}


fun isConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun TextView.setTopDrawable(drawable: Drawable) {
    setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
}

fun View.invert() {
    this.animate().setDuration(300).rotationBy(180f).start()
}

fun View.translate(value: Float, toShow: Boolean) {
    this.animate()
        .setDuration(300)
        .setListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator?) {
                this@translate.isVisible = toShow
            }

            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
        .translationYBy(value)
        .start()
}

fun Context.isFirstRun(): Boolean {
    val sharedPrefs = getSharedPrefs()
    return sharedPrefs.getBoolean("First", true)
}

fun Context.getSharedPrefs():SharedPreferences{
    return getSharedPreferences("hackerNews", Context.MODE_PRIVATE)
}