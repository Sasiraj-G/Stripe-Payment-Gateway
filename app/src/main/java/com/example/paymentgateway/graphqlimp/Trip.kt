package com.example.paymentgateway.graphqlimp
data class Trip(
    val id: String,
    val displayName : String,
    val title: String,
    val location: String,
    val dateRange: String,
    val price: String,
    val phone: String,
    val email: String,
    val imageUrl: String
)
