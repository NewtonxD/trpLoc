package abreuapps.trpLoc

import abreuapps.trpLoc.api.TrpAPIService
import abreuapps.trpLoc.api.model.RequestChangeStatusData
import abreuapps.trpLoc.api.model.RequestLocationData
import abreuapps.trpLoc.api.model.RequestVerifyData
import abreuapps.trpLoc.api.model.ResultVerifyData
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
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

class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob()+Dispatchers.IO)

    private var placa:String?=""

    private lateinit var locationClient: LocationClient

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
            ACTION_STOP -> stop(intent)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(intent: Intent?){

        placa = intent?.getStringExtra("placa")

        val notification = NotificationCompat.Builder(this,"location")
            .setContentTitle("Localizando "+placa!!)
            .setContentText("Loc: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient
            .getLocationUpdates(3000L)
            .catch { e->e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude.toString().takeLast(3)
                val lon = location.longitude.toString().takeLast(3)
                val updatedNotification = notification.setContentText(
                    "Loc: ($lat, $lon)"
                )
                sendData(
                    placa,
                    location.latitude.toString(),
                    location.longitude.toString()
                )
                notificationManager.notify(1,updatedNotification.build())
            }
            .launchIn(serviceScope)

        startForeground(1,notification.build())

    }

    private fun stop(intent: Intent?){

        placa = intent?.getStringExtra("placa")
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
        Toast.makeText(
            this,
            "Servicio detenido!",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object{
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    private fun sendData(
        placa:String?,
        lat: String,
        lon: String
    ){

        if (! placa.isNullOrBlank()){
            val api =
                Retrofit.Builder()
                    .baseUrl("http://192.168.100.76:8090")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            val retroAPI=api
                .create(TrpAPIService::class.java)

            val data = RequestLocationData(placa,lat,lon)

            val call = retroAPI.sendTransportInfo(data)

            call!!.enqueue(object: Callback<ResultVerifyData?> {
                override fun onResponse(p0: Call<ResultVerifyData?>, p1: Response<ResultVerifyData?>) {
                    
                }

                override fun onFailure(p0: Call<ResultVerifyData?>, p1: Throwable) {

                }
            })
        }
    }

    private fun changeStatus(
        placa:String?,
        estado:String
    ){
        if (! placa.isNullOrBlank()){
            val api =
                Retrofit.Builder()
                    .baseUrl("http://192.168.100.76:8090")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            val retroAPI=api
                .create(TrpAPIService::class.java)

            val data = RequestChangeStatusData(placa,estado)

            val call = retroAPI.changeTransportStatus(data)

            call!!.enqueue(object: Callback<ResultVerifyData?> {
                override fun onResponse(p0: Call<ResultVerifyData?>, p1: Response<ResultVerifyData?>) {

                }

                override fun onFailure(p0: Call<ResultVerifyData?>, p1: Throwable) {

                }
            })
        }
    }


}