package abreuapps.trpLoc

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.LocationServices

class LocationApp: Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            "location",
            "STP - Localización",
            NotificationManager.IMPORTANCE_LOW

        )
        val channel1 = NotificationChannel(
            "location error",
            "STP - ERROR CONEXION",
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        notificationManager.createNotificationChannel(channel1)
    }
}