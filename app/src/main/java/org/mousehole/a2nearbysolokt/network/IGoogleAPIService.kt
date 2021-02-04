package org.mousehole.a2nearbysolokt.network

import retrofit2.Call
import org.mousehole.a2nearbysolokt.model.MyPlaces
import org.mousehole.a2nearbysolokt.model.PlacesDetail
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface IGoogleAPIService {
    @GET
    fun getNearbyPlaces(@Url url:String): Call<MyPlaces>


    // This is to use service to fetch name and address for the place details (ViewPlace)

    @GET
    fun getDetailPlace(@Url url:String): Call<PlacesDetail>

    @GET("maps/api/directions/json")
    fun getDirections(@Query("origin") origin: String, @Query("destination") destination:String): Call<String>

}