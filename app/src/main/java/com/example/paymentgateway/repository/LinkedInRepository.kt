package com.example.paymentgateway.repository


import com.example.paymentgateway.models.LinkedInUser
import com.example.paymentgateway.network.RetrofitClient
import com.example.paymentgateway.utils.LinkedInConstants

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LinkedInRepository {

    suspend fun getAccessToken(authorizationCode: String): String {
        return withContext(Dispatchers.IO) {
            val response = RetrofitClient.linkedInAuthService.getAccessToken(
                code = authorizationCode,
                redirectUri = LinkedInConstants.REDIRECT_URI,
                clientId = LinkedInConstants.CLIENT_ID,
                clientSecret = LinkedInConstants.CLIENT_SECRET
            )
            return@withContext response.access_token
        }
    }

    suspend fun getUserProfile(accessToken: String): LinkedInUser {
        return withContext(Dispatchers.IO) {
            val profileResponse = RetrofitClient.linkedInApiService.getProfile(
                authorization = "Bearer $accessToken"
            )

            val emailResponse = RetrofitClient.linkedInApiService.getEmail(
                authorization = "Bearer $accessToken"
            )

            val email = if (emailResponse.elements.isNotEmpty()) {
                emailResponse.elements[0]._handle.emailAddress
            } else {
                ""
            }

            val profilePictureUrl = profileResponse.profilePicture?.displayImage?.elements?.firstOrNull()?.identifiers?.firstOrNull()?.identifier

            LinkedInUser(
                id = profileResponse.id,
                firstName = profileResponse.localizedFirstName,
                lastName = profileResponse.localizedLastName,
                email = email,
                profilePictureUrl = profilePictureUrl
            )
        }
    }
}