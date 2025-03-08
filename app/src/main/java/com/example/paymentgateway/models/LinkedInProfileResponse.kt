package com.example.paymentgateway.models

data class LinkedInProfileResponse(
    val id: String,
    val localizedFirstName: String,
    val localizedLastName: String,
    val profilePicture: ProfilePicture? = null
) {
    data class ProfilePicture(
        val displayImage: DisplayImage
    ) {
        data class DisplayImage(
            val elements: List<Element>
        ) {
            data class Element(
                val identifiers: List<Identifier>
            ) {
                data class Identifier(
                    val identifier: String
                )
            }
        }
    }
}
