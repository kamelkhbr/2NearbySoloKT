package org.mousehole.a2nearbysolokt.network

import retrofit2.Call
import org.mousehole.a2nearbysolokt.model.MyPlaces
import retrofit2.http.GET
import retrofit2.http.Url

interface IGoogleAPIService {
    @GET
    fun getNearbyPlaces(@Url url:String): Call<MyPlaces>
}