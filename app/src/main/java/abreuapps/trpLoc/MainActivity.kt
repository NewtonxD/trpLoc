package abreuapps.trpLoc

import abreuapps.trpLoc.api.TrpAPIService
import abreuapps.trpLoc.api.model.CheckedVal
import abreuapps.trpLoc.api.model.ErrorMessage
import abreuapps.trpLoc.api.model.InProcessVal
import abreuapps.trpLoc.api.model.LabelVal
import abreuapps.trpLoc.api.model.PlacaVal
import abreuapps.trpLoc.api.model.RequestVerifyData
import abreuapps.trpLoc.api.model.ResultVerifyData
import abreuapps.trpLoc.api.model.ValidTransportVal
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column{


            val label = remember{ LabelVal() }

            val errorMessage = remember { ErrorMessage() }

            val checkedVal = remember{ CheckedVal() }

            val inProcess = remember{ InProcessVal() }

            val placaVal = remember { PlacaVal() }

            val focusRequester = remember { FocusRequester() }

            val focusManager = LocalFocusManager.current

            val validTransport = remember{ ValidTransportVal() }

            //Text field column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
            ){

                Row (
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ){
                    Spacer(modifier = Modifier.padding(start = 16.dp, end = 16.dp))
                    OutlinedTextField(
                        value = placaVal.text,
                        label = { Text("Placa") },
                        singleLine = true,
                        onValueChange = {
                            placaVal.setVal(it)
                            if(errorMessage.text!=null) errorMessage.setVal(null)
                        },
                        enabled = ! checkedVal.value,
                        isError = errorMessage.text != null,
                        trailingIcon = {
                            if(errorMessage.text != null){
                                Icon(Icons.Filled.Warning, errorMessage.text)
                            }
                        },

                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .focusRequester(focusRequester)
                    )
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
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                inProcess.setVal(false)

                                if (! checkedVal.value) {

                                    if ( placaVal.text.trim().isEmpty() ) {

                                        validTransport.value.isValid=false
                                        validTransport.value.message="Placa requerida para proceder."
                                        errorMessage.setVal(validTransport.value.message)
                                        Toast.makeText( applicationContext, validTransport.value.message, Toast.LENGTH_SHORT).show()

                                    } else {

                                        try {
                                            validateVehicle(placaVal,validTransport.value)
                                        }catch (e: IOException){
                                            validTransport.value.message="No pudimos contactar al servidor!"
                                        }catch(e: HttpException){
                                            validTransport.value.message="No pudimos contactar al servidor!"
                                        }

                                        val validation = validTransport.value.isValid
                                        Toast.makeText( applicationContext, validTransport.value.message, Toast.LENGTH_SHORT).show()

                                        if(validation){
                                            errorMessage.setVal(null)
                                            label.setVal("Detener")
                                            checkedVal.setVal(true)
                                            Intent(applicationContext, LocationService::class.java).apply {
                                                action = LocationService.ACTION_START
                                                activity.startService(this)
                                            }
                                        }else{
                                            errorMessage.setVal(validTransport.value.message)
                                        }

                                    }



                                } else {

                                    label.setVal("Iniciar")
                                    checkedVal.setVal(false)
                                    Intent(applicationContext, LocationService::class.java).apply {
                                        action = LocationService.ACTION_STOP
                                        activity.stopService(this)
                                    }

                                }

                                inProcess.setVal(true)
                            },
                            modifier = Modifier.align(Alignment.CenterVertically),
                            enabled = inProcess.value


                        ){
                            Text(text = label.text)
                        }

                    }

                }


            }



        }
    }

}

private fun validateVehicle(
    placa:PlacaVal,
    res:ResultVerifyData
){
    val api =
        Retrofit.Builder()
            .baseUrl("http://192.168.100.76:8090")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val retroAPI=api
        .create(TrpAPIService::class.java)

    val data = RequestVerifyData(placa.text)

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
    //delaySeconds(1)
}

private fun delaySeconds( seconds:Int ) = runBlocking {
    delay( (seconds*1000).toLong() )
}