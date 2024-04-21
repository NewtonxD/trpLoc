package abreuapps.trpLoc.api.model

data class ResultVerifyData (
    var isValid:Boolean,
    var message:String,
    var token:String
)