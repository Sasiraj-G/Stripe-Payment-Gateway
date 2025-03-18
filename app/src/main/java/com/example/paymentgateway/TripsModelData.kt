package com.example.paymentgateway

data class TripsModelData(
    val id: Int,
    val name : String,
    val price : String,
    val location : String,
    val address : String,
    val startEndDate : String,
    val gmailId : String
)

fun sampleData() = mutableListOf(
    TripsModelData(1,"John","100000","Chennai","12 Madurai","13-13","john@gmail.com"),
    TripsModelData(2,"Robert","100000","Chennai","12 Madurai","13-13","john@gmail.com"),
    TripsModelData(3,"Honkey","100000","Chennai","12 Madurai","13-13","john@gmail.com"),
    TripsModelData(4,"Cena","100000","Chennai","12 Madurai","13-13","cena@gmail.com"),
)

