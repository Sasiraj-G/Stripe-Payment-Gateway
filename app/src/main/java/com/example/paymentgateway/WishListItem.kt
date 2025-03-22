package com.example.paymentgateway


//data class WishlistItem(
//    val id: String,
//    val image: String? = null,
//    val count: Int = 0,
//    val isHeartVisible: Boolean = true
//)

data class WishlistItem(
    val name: String,
    val count: Int,
    val isPlaceholder: Boolean
)