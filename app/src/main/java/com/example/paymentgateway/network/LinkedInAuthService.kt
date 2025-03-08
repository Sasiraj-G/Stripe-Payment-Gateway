package com.example.paymentgateway.network

import com.example.paymentgateway.models.LinkedInAccessTokenResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LinkedInAuthService {
    @FormUrlEncoded
    @POST("oauth/v2/accessToken")
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): LinkedInAccessTokenResponse
}