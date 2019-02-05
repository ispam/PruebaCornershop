package com.pruebacornershop.Data.Remote

import com.pruebacornershop.Data.Local.Entities.Counter
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*

interface APIService {

    @GET("api/v1/counters")
    fun getCountersList(): Single<List<Counter>>

    @POST("api/v1/counter")
    fun createCounter(@Body counter: Counter): Single<List<Counter>>

    @POST("api/v1/counter/inc")
    fun incrementCounter(): Single<List<Counter>>

    @POST("api/v1/counter/dec")
    fun subtractCounter(): Single<List<Counter>>

    @DELETE("api/v1/counter")
    fun deleteCounter(): Single<List<Counter>>
}