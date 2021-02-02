package org.mousehole.a2nearbysolokt.common

import org.mousehole.a2nearbysolokt.model.Results
import org.mousehole.a2nearbysolokt.network.IGoogleAPIService
import org.mousehole.a2nearbysolokt.network.RetrofitClient


// This is the util constants file

object Common {

    private val GOOGLE_API_URL="https://maps.googleapis.com/"

    var currentResult: Results?=null


    val googleAPIService: IGoogleAPIService
    get() = RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)




}