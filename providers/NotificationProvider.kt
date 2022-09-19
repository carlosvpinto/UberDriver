package com.carlosvicente.uberdriverkotlin.providers

import com.carlosvicente.uberdriverkotlin.api.IFCMApi
import com.carlosvicente.uberdriverkotlin.api.RetrofitClient
import com.carlosvicente.uberdriverkotlin.models.FCMBody
import com.carlosvicente.uberdriverkotlin.models.FCMResponse
import retrofit2.Call

class NotificationProvider {

    private val URL = "https://fcm.googleapis.com"

    fun sendNotification(body: FCMBody): Call<FCMResponse> {
        return RetrofitClient.getClient(URL).create(IFCMApi::class.java).send(body)
    }

}