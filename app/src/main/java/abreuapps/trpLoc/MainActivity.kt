package abreuapps.trpLoc

import abreuapps.trpLoc.ui.theme.trpLocTheme
import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val access:Array<String> = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        ActivityCompat.requestPermissions(
            this,
            access,
            0
        )

        setContent {
            trpLocTheme{
                MainUI(applicationContext,this)
            }

        }
    }
}

@Composable
fun MainUI(applicationContext: Context,activity: MainActivity){

    val label:MutableState<String> = remember {
        mutableStateOf("Iniciar Localizador")
    }
    val checkedVal:MutableState<Boolean> = remember {
        mutableStateOf(false)
    }

    val placaVal:MutableState<String> = remember {
        mutableStateOf("")
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
                            checkedVal.value = it
                            if (it) {
                                label.value = "Detener Localizador"
                                Intent(applicationContext, LocationService::class.java).apply {
                                    action = LocationService.ACTION_START
                                    activity.startService(this)
                                }
                            } else {
                                label.value = "Iniciar Localizador"
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