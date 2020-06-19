package com.benmohammad.newzz.topnewsalert

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat.*
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.benmohammad.newzz.R
import com.benmohammad.newzz.data.model.Item
import com.benmohammad.newzz.data.remote.HNApiClient
import com.benmohammad.newzz.ui.newsdetails.NewsDetailActivity
import com.benmohammad.newzz.util.LOGO_URL
import com.benmohammad.newzz.util.getSafeResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class TopNewsFetchWorker(appContext: Context, workerParameters: WorkerParameters): Worker(appContext, workerParameters), CoroutineScope {

    private val notifyId = 1
    private val notifyGroup = "notify group"


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO



    private val client = HNApiClient(appContext).hnApiService

    override fun doWork(): Result {

        return runBlocking {
            try {
                val itemIds = getSafeResponse(client.topStories())
                val topItems = mutableListOf<Item>()
                if(itemIds.isNotEmpty()) {
                    for(i in 0 until 3) {
                        val item = client.getItem(itemIds[Random.nextInt(0, itemIds.size)]).body()
                        item?.let{
                            topItems.add(item)
                        }
                    }
                }
                generateTopAlerts(applicationContext, topItems)
            } catch (e: Exception) {
                when(e) {
                    is UnknownHostException,
                        is ConnectException,
                        is SocketTimeoutException -> {
                        return@runBlocking Result.retry()
                    }
                    else -> throw e
                }
            }
            Result.success()
        }
    }

    private fun generateTopAlerts(appContext: Context, topItems: List<Item>) {
        val nm = NotificationManagerCompat.from(appContext)
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    "Top Alerts",
                    "Top Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
        val notifyList = mutableListOf<Notification>()
        for(item in topItems) {

            val intent = Intent(appContext, NewsDetailActivity::class.java).apply {
                putExtra("LogoUrl", "$LOGO_URL${item.domain}")
                putExtra("url", item.url)
                putExtra("author", item.by)
                putExtra("itemObj", item)
            }

            val pi = PendingIntent.getActivity(appContext, item.id.toInt(), intent, PendingIntent.FLAG_ONE_SHOT)
            val notif = Builder(appContext, "Top Alerts").apply {
                setContentTitle(
                    if(item.domain != "nill") {
                        item.domain.removeSuffix("/")
                    } else {
                        "Hacker News"
                    }
                )

                setStyle(
                    BigTextStyle()
                        .setBigContentTitle(
                            if(item.domain != "nill") {
                                item.domain.removeSuffix("/")
                            } else {
                                "Hacker News"
                            }
                        )
                        .bigText(item.title)
                )

                setAutoCancel(true)
                setContentText(item.title)
                setContentIntent(pi)
                setGroup(notifyGroup)
                setSmallIcon(R.drawable.ic_news_notif)
                color = ContextCompat.getColor(appContext, R.color.colorAccent)
            }.build()

            notifyList.add(notif)
        }
        val summaryNotif = Builder(appContext, "Top Alerts").apply {
            setContentTitle("Top Alerts")
            setContentText("3 New Alerts")
            setSmallIcon(R.drawable.ic_news_notif)
            setStyle(
                InboxStyle()
                    .addLine(topItems[0].title)
                    .addLine(topItems[1].title)
                    .addLine(topItems[2].title)
                    .setBigContentTitle("3 New Alerts")
                    .setSummaryText("Hacker News Top Alert")
            )
            setGroup(notifyGroup)
            setGroupSummary(true)
            color = ContextCompat.getColor(appContext, R.color.colorAccent)
            priority = PRIORITY_MAX

        }.build()

        nm.apply {
            notify(topItems[0].id.toInt(), notifyList[0])
            notify(topItems[1].id.toInt(), notifyList[1])
            notify(topItems[2].id.toInt(), notifyList[4])
            notify(notifyId, summaryNotif)
        }
    }
}