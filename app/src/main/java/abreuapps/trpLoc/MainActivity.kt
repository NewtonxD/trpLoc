package abreuapps.trpLoc

import abreuapps.trpLoc.api.TrpAPIService
import abreuapps.trpLoc.api.model.RequestVerifyData
import abreuapps.trpLoc.api.model.ResultVerifyData
import abreuapps.trpLoc.ui.theme.trpLocTheme
import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val access:Array<String> = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
        )

        ActivityCompat.requestPermissions(
            this,
            access,
            0
        )

        setContent {
            trpLocTheme{
                MainUI(
                    applicationContext,
                    this
                )
            }

        }
    }
}

@Composable
fun MainUI(
    applicationContext: Context,
    activity: MainActivity
    ){

    val label:MutableState<String> = remember {
        mutableStateOf("Iniciar Localizador")
    }
    val checkedVal:MutableState<Boolean> = remember {
        mutableStateOf(false)
    }

    val placaVal:MutableState<String> = remember {
        mutableStateOf("")
    }

    val validTransport=remember{
        mutableStateOf(ResultVerifyData(false,""))
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column{


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
            ){

                Row (
                    modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                ){
                    Text(
                        text = "Placa:",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    OutlinedTextField(
                        value = placaVal.value,
                        onValueChange = {
                            placaVal.value=it
                        },
                        enabled = ! checkedVal.value,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                }


            }

            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
            ){
                Row (
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ){
                    Text(
                        text = label.value,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    Switch(
                        checked = checkedVal.value,
                        onCheckedChange = {
                            if (it) {
                                validateVehicle(placaVal.value,validTransport)
                                val validation = validTransport.value.isValid
                                Toast.makeText( applicationContext, validTransport.value.message, Toast.LENGTH_LONG).show()

                                if(validation){
                                    label.value = "Detener Localizador"
                                    checkedVal.value = true
                                    Intent(applicationContext, LocationService::class.java).apply {
                                        action = LocationService.ACTION_START
                                        activity.startService(this)
                                    }
                                }

                            } else {
                                label.value = "Iniciar Localizador"
                                checkedVal.value = false
                                Intent(applicationContext, LocationService::class.java).apply {
                                    action = LocationService.ACTION_STOP
                                    activity.stopService(this)
                                }
                            }

                        },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                }

            }
        }
    }

}

private fun validateVehicle(
    placa:String,
    res:MutableState<ResultVerifyData>
):Unit {

    val api =
        Retrofit.Builder()
            .baseUrl("http://192.168.100.76:8090")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val retroAPI=api
        .create(TrpAPIService::class.java)

    val data = RequestVerifyData(placa)

    val call: Call<ResultVerifyData?>? = retroAPI.validateInfo(data)

    call!!.enqueue(object: Callback<ResultVerifyData?>{
        override fun onResponse(p0: Call<ResultVerifyData?>, p1: Response<ResultVerifyData?>) {
            res.value=p1.body()!!
        }

        override fun onFailure(p0: Call<ResultVerifyData?>, p1: Throwable) {
            res.value.message="Error: No pudimos verificar Transporte..."
            res.value.isValid=false
        }
    })

}
