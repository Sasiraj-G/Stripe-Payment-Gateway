package com.example.paymentgateway

data class SimilarListData(
    val image : String,
    val currency : String,
    val description : String,
    val roomType : String,
)

fun getSimilarListings(): List<SimilarListData>  = listOf(
    SimilarListData("https://staging1.flutterapps.io/images/upload/x_medium_c2951bcf126816850b377ea63d886682.png","$500.0","Thousand hils ketambe sumatra","Hotel Room / 3 beds"),
    SimilarListData("https://staging1.flutterapps.io/images/upload/x_medium_c2951bcf126816850b377ea63d886682.png","$250.0","Thousand hils ketambe sumatra","Smart Room / 2 beds"),
    SimilarListData("https://staging1.flutterapps.io/images/upload/x_medium_c2951bcf126816850b377ea63d886682.png","$1800.0","Thousand hils ketambe sumatra","House Room / 4 beds")
)