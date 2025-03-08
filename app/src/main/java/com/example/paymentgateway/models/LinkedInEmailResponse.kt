package com.example.paymentgateway.models

data class LinkedInEmailResponse(
    val elements: List<Element>
) {

    data class Element(
        val handle: String,
        val _handle: HandleDetail
    ) {
        data class HandleDetail(
            val emailAddress: String
        )
    }
}
