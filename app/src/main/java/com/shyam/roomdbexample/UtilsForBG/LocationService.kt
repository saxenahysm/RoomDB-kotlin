package com.shyam.roomdbexample.UtilsForBG

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.gms.location.LocationServices
import com.shyam.roomdbexample.Book
import com.shyam.roomdbexample.BookDao
import com.shyam.roomdbexample.BookDatabase
import com.shyam.roomdbexample.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocationService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient
    private lateinit var bookDao: BookDao

    @RequiresApi(Build.VERSION_CODES.O)
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @RequiresApi(Build.VERSION_CODES.O)
    val current: String = LocalDateTime.now().format(formatter)
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext, LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        val db = Room.databaseBuilder(
            applicationContext, BookDatabase::class.java, "book_database"
        ).build()
        bookDao = db.bookDao()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun start() {
        val notification =
            NotificationCompat.Builder(this, "location").setContentText("Location:null")
                .setContentTitle("Track-location-Test").setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationClient.getLocationUpdates(500L).catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude.toString()
                val lng = location.longitude.toString()
                val updatedNotification = notification.setContentText("Location: ($lat,$lng)")
                notificationManager.notify(1, updatedNotification.build())
                Log.e("TAG111111", "lat--- $lat : lng--- $lng")
                insertData(lat,lng)
            }.launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData(lat:String,lng:String) {
        //Insert
        Log.i("MyTAG", "lat-- $lat \t lng---$lng current--$current")
        bookDao.insertBook(Book(0, lat, lng, current))
        Log.i("MyTAG", "*****     Inserted      **********")
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}


