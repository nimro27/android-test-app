package com.example.testapp

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Path

data class Credentials(val username: String, val password: String)

interface UnifiApiService {
    @POST("/api/login")
    @Headers("Content-Type: application/json")
    fun login(@Body credentials: Credentials): Call<ResponseBody>

    @GET("/api/s/{site}/self")
    fun getStatus(@Path("site") site: String): Call<ResponseBody>

    @GET("/api/s/{site}/stat/sta")
    fun getConnectedDevices(@Path("site") site: String): Call<ResponseBody>
}