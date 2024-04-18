package abreuapps.trpLoc.api

import abreuapps.trpLoc.api.model.RequestLocationData
import abreuapps.trpLoc.api.model.RequestVerifyData
import abreuapps.trpLoc.api.model.ResultVerifyData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TrpAPIService {
    @Headers("Content-Type: application/json")
    @POST("API/trp/verifyTrpData")
    fun validateInfo(
       @Body placa : RequestVerifyData?
    ) : Call<ResultVerifyData?>?

    @Headers("Content-Type: application/json")
    @POST("API/trp/sendTrpData")
    fun sendTransportInfo(
        @Body data : RequestLocationData
    ) : Call<ResultVerifyData?>?

}