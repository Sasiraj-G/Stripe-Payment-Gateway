package com.example.paymentgateway.network

import com.example.paymentgateway.models.LinkedInEmailResponse
import com.example.paymentgateway.models.LinkedInProfileResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface LinkedInApiService {
    @GET("v2/me")
    suspend fun getProfile(
        @Header("auth") authorization: String
    ): LinkedInProfileResponse

    @GET("v2/emailAddress?q=members&projection=(elements*(handle~))")
    suspend fun getEmail(
        @Header("auth") authorization: String
    ): LinkedInEmailResponse
}