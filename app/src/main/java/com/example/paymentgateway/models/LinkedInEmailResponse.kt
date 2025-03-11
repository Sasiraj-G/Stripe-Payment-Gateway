package com.example.paymentgateway.models

import com.google.gson.annotations.SerializedName

data class LinkedInEmailResponse(
    val elements: List<Element>
) {

    data class Element(
        val handle: String,
        @SerializedName("handle~")
        val handleDetails: HandleDetail
    ) {
        data class HandleDetail(
            val emailAddress: String
        )
    }
}
