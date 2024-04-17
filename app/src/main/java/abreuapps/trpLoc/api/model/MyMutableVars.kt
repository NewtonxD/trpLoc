package abreuapps.trpLoc.api.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PlacaVal() {
    var text by mutableStateOf("")
        private set
    fun setVal(text: String) {
        this.text = text
    }
}

class ErrorMessage(){
    var text:String? by mutableStateOf(null)
        private set
    fun setVal(text: String?) {
        this.text = text
    }
}

class LabelVal(){
    var text:String by mutableStateOf("Iniciar")
        private set
    fun setVal(text: String) {
        this.text = text
    }
}

class CheckedVal(){
    var value:Boolean by mutableStateOf(false)
        private set
    fun setVal(text: Boolean) {
        this.value = text
    }
}

class InProcessVal(){
    var value:Boolean by mutableStateOf(true)
        private set
    fun setVal(text: Boolean) {
        this.value = text
    }
}

class ValidTransportVal(){

    var value : ResultVerifyData by mutableStateOf( ResultVerifyData(false,"") )
        private set
    fun setVal(text: ResultVerifyData) {
        this.value = text
    }


}