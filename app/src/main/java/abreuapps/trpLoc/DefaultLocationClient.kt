package abreuapps.trpLoc

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
): LocationClient
{
    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            if( !context.hasLocationPermission() )
                throw LocationClient.LocationException("No tenemos permisos de localización!")

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if( !isGpsEnabled && !isNetworkEnabled )
                throw LocationClient.LocationException("GPS está inhabilitado!")

            val request = LocationRequest.Builder(interval)
                .setMinUpdateDistanceMeters(0f)
                .setIntervalMillis(interval)
                .setWaitForAccurateLocation(true)
                .build()

            val locationCallback = object : LocationCallback(){
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    p0.locations.lastOrNull()?.let{
                        location -> launch { send(location) }
                    }
                }
            }

            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose{
                client.removeLocationUpdates(locationCallback)
            }


        }
    }
}
