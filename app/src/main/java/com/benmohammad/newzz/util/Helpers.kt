package com.benmohammad.newzz.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.widget.TextView
import retrofit2.Response


val LOGO_URL = "https://logo.clearbit.com/"

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