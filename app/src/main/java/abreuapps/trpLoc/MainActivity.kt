package abreuapps.trpLoc

import abreuapps.trpLoc.api.TrpAPIService
import abreuapps.trpLoc.api.model.RequestChangeStatusData
import abreuapps.trpLoc.api.model.RequestVerifyData
import abreuapps.trpLoc.api.model.ResultVerifyData
import abreuapps.trpLoc.ui.theme.AppTheme
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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

        val baseIP=getString(R.string.api_key)

        ActivityCompat.requestPermissions(
            this,
            access,
            0
        )

        val dummy_rutas= listOf(
            "RUTA A",
            "RUTA B",
            "RUTA 2"
        )

        setContent {
            AppTheme{

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainUI(
                        applicationContext,
                        this,
                        baseIP,
                        dummy_rutas
                    )
                }
            }

        }
    }
}


@Composable
fun MainUI(
    applicationContext: Context,
    activity: MainActivity,
    baseIP: String,
    rutasList: List<String>
    ){

    val errorMessage = rememberSaveable{ mutableStateOf("") }

    val enServicio = rememberSaveable{ mutableStateOf(false) }

    val placaVal = rememberSaveable{ mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    val focusManager = LocalFocusManager.current

    val selectedOptText = rememberSaveable { mutableStateOf(rutasList[0]) }

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
                        label = { Text("Placa")},
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
                        enabled= ! enServicio.value,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .focusRequester(focusRequester)

                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
            ) {
                //start
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ) {

                    DynamicSelectTextField(
                        "Ruta",
                        selectedOptText,
                        rutasList,
                        enServicio,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
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
                                    baseIP,
                                    placaVal,
                                    errorMessage,
                                    enServicio
                                )


                            }

                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                        enabled = ! enServicio.value,
                        modifier = Modifier.align(Alignment.CenterVertically)

                    ) {
                        Text(text = "Iniciar", color = Color.White)
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
                                    baseIP,
                                    placaVal,
                                    "Estacionado",
                                    enServicio
                                )
                            }

                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(text = "Detener", color = Color.White)
                    }

                }

            }

        }


    }


}

private fun validateVehicle(
    context: Context,
    activity: MainActivity,
    baseIP: String,
    placa:MutableState<String>,
    errorMessage:MutableState<String>,
    enServicio:MutableState<Boolean>
){


    val pwd = "*Dd123456"

    val api =
        Retrofit.Builder()
            .baseUrl(baseIP)
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
                    enServicio.value   = true
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicSelectTextField(
    label: String,
    selectedValue: MutableState<String>,
    options: List<String>,
    enServicio: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val expanded = remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = !expanded.value },
        modifier = modifier
    ) {
        OutlinedTextField(
            readOnly = true,
            enabled = ! enServicio.value,
            value = selectedValue.value,
            onValueChange = {},
            label = { Text(text = label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value)
            },
            modifier = Modifier
                .menuAnchor()
        )

        ExposedDropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false },) {
            options.forEach { option: String ->
                DropdownMenuItem(
                    text = { Text(text = option, color =  if(selectedValue.value==option) Color.White else Color.LightGray  ) },
                    onClick = {
                        expanded.value = false
                        selectedValue.value=option
                    }
                )
            }
        }
    }
}



private fun changeStatus(
    context: Context,
    activity: MainActivity,
    baseIP:String,
    placa: MutableState<String>,
    estado:String,
    enServicio: MutableState<Boolean>
){
    val pwd = "*Dd123456"
    val api =
        Retrofit.Builder()
            .baseUrl(baseIP)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val retroAPI=api
        .create(TrpAPIService::class.java)

    val data = RequestChangeStatusData(placa.value,estado,pwd)

    val call = retroAPI.changeTransportStatus(data)

    call!!.enqueue(object: Callback<ResultVerifyData?>{
        override fun onResponse(p0: Call<ResultVerifyData?>, p1: Response<ResultVerifyData?>) {
            if(p1.isSuccessful && p1.body()!=null){

                enServicio.value   = false

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