package com.benmohammad.newzz.util

import android.content.Context
import android.net.ConnectivityManager
import retrofit2.Response


val LOGO_URL = "https://logo.clearbit.com/"

fun <T>getSafeResponse(response: Response<List<T>>): List<T> {
    return if(response.isSuccessful) {
        response.body() ?: emptyList()
    } else {
        throw Errors.FetchException(response.errorBody()?.string() ?: "Unknown Error")
    }
}

fun isConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}