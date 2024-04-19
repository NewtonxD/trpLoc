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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


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

    var errorMessage by remember{ mutableStateOf("") }.apply { value=this.value }

    var placaVal by remember{ mutableStateOf("") }.apply { value=this.value }

    val focusRequester = remember { FocusRequester() }

    val focusManager = LocalFocusManager.current

    val validTransport by remember{ mutableStateOf( ResultVerifyData(false,"") ) }.apply { value=this.value }

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
                        value = placaVal,
                        label = { Text("Placa") },
                        singleLine = true,
                        onValueChange = {
                            placaVal=it
                            if (errorMessage != "") errorMessage=""
                        },
                        isError = errorMessage != "",
                        trailingIcon = {
                            if (errorMessage != "") {
                                Icon(Icons.Filled.Warning, errorMessage)
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

                            if (placaVal.trim().isEmpty()) {

                                validTransport.message = "Placa requerida para proceder."
                                errorMessage = validTransport.message
                                Toast.makeText(
                                    applicationContext,
                                    validTransport.message,
                                    Toast.LENGTH_SHORT
                                ).show()

                            } else {

                                try {
                                    validateVehicle(placaVal, validTransport)
                                } catch (e: IOException) {
                                    validTransport.message =
                                        "No pudimos contactar al servidor!"
                                } catch (e: HttpException) {
                                    validTransport.message =
                                        "No pudimos contactar al servidor!"
                                }

                                val validation = validTransport.isValid

                                Toast.makeText(
                                    applicationContext,
                                    validTransport.message,
                                    Toast.LENGTH_SHORT
                                ).show()

                                if (validation) {
                                    errorMessage = ""
                                    Intent(
                                        applicationContext,
                                        LocationService::class.java
                                    ).apply {
                                        putExtra("placa",placaVal)
                                        putExtra("token",token)

                                        action = LocationService.ACTION_START
                                        activity.startService(this)
                                    }
                                } else {
                                    errorMessage = validTransport.message
                                }
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

                            Intent(
                                applicationContext,
                                LocationService::class.java
                            ).apply {
                                if(this.getStringExtra("placa").isNullOrBlank())
                                    putExtra("placa",placaVal)

                                if(this.getStringExtra("token").isNullOrBlank())
                                    putExtra("token",token)

                                action = LocationService.ACTION_STOP
                                activity.stopService(this)
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
    placa:String,
    res:ResultVerifyData
){
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
            if(p1.isSuccessful && p1.body()!=null){
                res.message=p1.body()!!.message
                res.isValid=p1.body()!!.isValid
            }
        }

        override fun onFailure(p0: Call<ResultVerifyData?>, p1: Throwable) {
            res.message="No pudimos contactar al servidor!"
            res.isValid=false
        }
    })
}