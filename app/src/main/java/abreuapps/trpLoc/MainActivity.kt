package abreuapps.trpLoc

import abreuapps.trpLoc.api.TrpAPIService
import abreuapps.trpLoc.api.model.RequestChangeStatusData
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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

    val errorMessage = remember{ mutableStateOf("") }

    val placaVal = remember{ mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column {

            //Text field column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
            ) {

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ) {
                    OutlinedTextField(
                        value = placaVal.value,
                        label = { Text("Placa") },
                        singleLine = true,
                        onValueChange = {
                            placaVal.value=it
                            if (errorMessage.value != "") errorMessage.value=""
                        },
                        isError = errorMessage.value != "",
                        trailingIcon = {
                            if (errorMessage.value!= "") {
                                Icon(Icons.Filled.Warning, errorMessage.value)
                            }
                        },

                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .focusRequester(focusRequester)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                //start
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()

                            if (placaVal.value.trim().isEmpty()) {
                                errorMessage.value = "Placa requerida para proceder."
                                Toast.makeText(
                                    applicationContext,
                                    "Placa requerida para proceder.",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } else {
                                validateVehicle(
                                    applicationContext,
                                    activity,
                                    placaVal,
                                    errorMessage
                                )


                            }

                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                        modifier = Modifier.align(Alignment.CenterVertically)

                    ) {
                        Text(text = "Iniciar")
                    }

                }
                //Stop
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()

                            if (placaVal.value.trim().isEmpty()) {
                                errorMessage.value = "Placa requerida para proceder."
                                Toast.makeText(
                                    applicationContext,
                                    "Placa requerida para proceder.",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } else {
                                changeStatus(
                                    applicationContext,
                                    activity,
                                    placaVal,
                                    "Estacionado"
                                )
                            }

                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.align(Alignment.CenterVertically)

                    ) {
                        Text(text = "Detener")
                    }

                }

            }


        }


    }


}

private fun validateVehicle(
    context: Context,
    activity: MainActivity,
    placa:MutableState<String>,
    errorMessage:MutableState<String>
){


    val pwd = "*Dd123456"

    val api =
        Retrofit.Builder()
            .baseUrl("http://192.168.100.76:8090")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val retroAPI=api
        .create(TrpAPIService::class.java)

    val data = RequestVerifyData(placa.value,pwd)

    val call: Call<ResultVerifyData?>? = retroAPI.validateInfo(data)

    call!!.enqueue(object: Callback<ResultVerifyData?>{
        override fun onResponse(p0: Call<ResultVerifyData?>, p1: Response<ResultVerifyData?>) {
            if(p1.isSuccessful && p1.body()!=null){

                val validation = p1.body()!!.isValid

                if(p1.body()!!.message.isNotBlank())
                    Toast.makeText(
                        context,
                        p1.body()!!.message,
                        Toast.LENGTH_SHORT
                    ).show()


                if (validation) {
                    errorMessage.value = ""
                    Intent(
                        context,
                        LocationService::class.java
                    ).apply {
                        removeExtra("token")
                        removeExtra("placa")
                        putExtra("placa", placa.value)
                        putExtra("token", p1.body()!!.token)

                        action = LocationService.ACTION_START
                        activity.startService(this)
                    }
                } else {
                    errorMessage.value = p1.body()!!.message
                }

            }
        }

        override fun onFailure(p0: Call<ResultVerifyData?>, p1: Throwable) {
            Toast.makeText(
                context,
                "No pudimos contactar al servidor!",
                Toast.LENGTH_SHORT
            ).show()
        }
    })
}



private fun changeStatus(
    context: Context,
    activity: MainActivity,
    placa: MutableState<String>,
    estado:String
){
    val pwd = "*Dd123456"
    val api =
        Retrofit.Builder()
            .baseUrl("http://192.168.100.76:8090")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val retroAPI=api
        .create(TrpAPIService::class.java)

    val data = RequestChangeStatusData(placa.value,estado,pwd)

    val call = retroAPI.changeTransportStatus(data)

    call!!.enqueue(object: Callback<ResultVerifyData?>{
        override fun onResponse(p0: Call<ResultVerifyData?>, p1: Response<ResultVerifyData?>) {
            if(p1.isSuccessful && p1.body()!=null){

                if(p1.body()!!.isValid){
                    Intent(
                        context,
                        LocationService::class.java
                    ).apply {

                        action = LocationService.ACTION_STOP
                        activity.stopService(this)



                    }
                }

                var txtAux=""
                if(p1.body()!!.isValid) txtAux=" Deteniendo Servicio..."

                Toast.makeText(
                    context,
                    p1.body()!!.message+txtAux,
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

        override fun onFailure(p0: Call<ResultVerifyData?>, p1: Throwable) {
            Toast.makeText(
                context,
                "No pudimos contactar al servidor!",
                Toast.LENGTH_SHORT
            ).show()
        }
    })



}