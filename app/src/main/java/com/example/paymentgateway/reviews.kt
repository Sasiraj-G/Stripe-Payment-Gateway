package com.example.paymentgateway

data class ReviewModel(
    val name : String,
    val description : String,
    val date : String
)

fun getReviews(): List<ReviewModel> = listOf(
    ReviewModel("James Review","That was a great stay !!","February, 2025"),
    ReviewModel("James Review","Grate stay with great facilities !!","February, 2025"),
    ReviewModel("John Review","Grate stay with very relaxing and chill","February, 2025")
)