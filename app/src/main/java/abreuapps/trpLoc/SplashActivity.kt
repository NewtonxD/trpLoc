package abreuapps.trpLoc

import abreuapps.trpLoc.ui.theme.AppTheme
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity: ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            AppTheme{
                SplashScreen()
            }
        }
    }

    @Composable
    private fun SplashScreen(){
        val alpha = remember {
            Animatable(0f)
        }

        LaunchedEffect(key1 = true, block = {
            alpha.animateTo(1f, animationSpec = tween(1500))
            delay(2000)
            startActivity(Intent(this@SplashActivity,MainActivity::class.java))
        })
        Box(modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center){
                Image(painter = painterResource(id = R.drawable.omsalogo), contentDescription = null)
        }
    }
}