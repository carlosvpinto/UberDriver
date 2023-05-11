package com.carlosvicente.uberdriverkotlin.models

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Driver (
    val id: String? = null,
    val name: String? = null,
    val lastname: String? = null,
    val email: String? = null,
    val phone: String? = null,
    var image: String? = null,
    val plateNumber: String? = null,
    val colorCar: String? = null,
    val brandCar: String? = null,
    val tipo: String? = null,
    val activado:Boolean? = false,
    var billetera: Double? = null,
    val disponible:Boolean? = false

) {


    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Client>(json)
    }
}