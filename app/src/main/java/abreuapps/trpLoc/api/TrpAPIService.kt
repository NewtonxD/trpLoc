package abreuapps.trpLoc.api

import abreuapps.trpLoc.api.model.RequestChangeStatusData
import abreuapps.trpLoc.api.model.RequestLocationData
import abreuapps.trpLoc.api.model.RequestVerifyData
import abreuapps.trpLoc.api.model.ResultVerifyData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TrpAPIService {
    @Headers("Content-Type: application/json")
    @POST("API/trp/verifyData")
    fun validateInfo(
       @Body placa : RequestVerifyData?
    ) : Call<ResultVerifyData?>?

    @Headers("Content-Type: application/json")
    @POST("API/trp/sendData")
    fun sendTransportInfo(
        @Body data : RequestLocationData
    ) : Call<ResultVerifyData?>?

    @Headers("Content-Type: application/json")
    @POST("API/trp/changeStatus")
    fun changeTransportStatus(
        @Body data : RequestChangeStatusData
    ) : Call<ResultVerifyData?>?

}