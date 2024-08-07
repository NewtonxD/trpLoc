package abreuapps.trpLoc

import abreuapps.trpLoc.api.TrpAPIService
import abreuapps.trpLoc.api.model.RequestLocationData
import abreuapps.trpLoc.api.model.ResultVerifyData
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar

class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob()+Dispatchers.IO)

    private var placa:String?=""

    private var token:String?=""

    private lateinit var locationClient: LocationClient

    private var intentos=0;

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action){
            ACTION_START -> start(intent)
            ACTION_STOP -> stop()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(intent: Intent?){
        placa = intent?.getStringExtra("placa")
        token = intent?.getStringExtra("token")
        val notification = NotificationCompat.Builder(this,"location")
            .setContentTitle("Localizando "+placa!!)
            .setContentText("Loc: null")
            .setSmallIcon(R.drawable.omsalogo)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var currentSecond: Int = Calendar.getInstance().get(Calendar.SECOND)
        while ( currentSecond%5!=0 ) {
            Thread.sleep(1000)
            currentSecond = Calendar.getInstance().get(Calendar.SECOND)
        }

        locationClient
            .getLocationUpdates(5000L)
            .catch { e->e.printStackTrace()
                intentos++
                if(intentos>=12){

                    val notificationManager1 = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    val notification1=NotificationCompat.Builder(applicationContext,"location error")
                        .setContentTitle("Servicio detenido automáticamente.")
                        .setContentText("Revise el Internet y luego reinicie la aplicación.")
                        .setSmallIcon(R.drawable.omsalogo)

                    notificationManager1.notify(2,notification1.build())

                    stop()

                }

            }
            .onEach { location ->
                val lat = location.latitude.toString().takeLast(3)
                val lon = location.longitude.toString().takeLast(3)
                val updatedNotification = notification.setContentText(
                    "Loc: ($lat, $lon)"
                )
                sendData(
                    placa,
                    location.latitude.toString(),
                    location.longitude.toString(),
                    token
                )
                notificationManager.notify(1,updatedNotification.build())
            }
            .launchIn(serviceScope)

        startForeground(1,notification.build())

    }

    @Suppress("DEPRECATION")
    private fun stop(){
        Handler(Looper.getMainLooper()).post {
            stopForeground(true)
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object{
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    private fun sendData(
        placa:String?,
        lat: String,
        lon: String,
        token:String?
    ){
        if(intentos>=12){

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notification=NotificationCompat.Builder(this,"location error")
                .setContentTitle("Servicio detenido automáticamente.")
                .setContentText("Revise el Internet y luego reinicie la aplicación.")
                .setSmallIcon(R.drawable.omsalogo)

            notificationManager.notify(2,notification.build())

            stop()

        }else{
            val api =
                Retrofit.Builder()
                    .baseUrl(getString(R.string.api_key))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            val retroAPI=api
                .create(TrpAPIService::class.java)

            val data = RequestLocationData(placa!!,lat,lon,token!!)

            val call = retroAPI.sendTransportInfo(data)

            call!!.enqueue(object: Callback<ResultVerifyData?>{
                override fun onResponse(p0: Call<ResultVerifyData?>, p1: Response<ResultVerifyData?>) {
                    if(p1.body()!!.isValid){
                        intentos=0
                    }else{
                        intentos++
                    }
                }

                override fun onFailure(p0: Call<ResultVerifyData?>, p1: Throwable) {
                    intentos++
                }
            })
        }
    }


}