package abreuapps.trpLoc.api

import abreuapps.trpLoc.api.model.ResultVerifyData
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.Query

interface TrpAPIService {

    @POST("/verifyTrpData")
    fun validateInfo(
       @Query("placa") placa : String
    ) : Call<ResultVerifyData>

}