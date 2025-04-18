package com.example.paymentgateway

import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterface {
    @Headers("Authorization: Bearer ${Utils.SECRET_KEY}")
    @POST("v1/customers")
    suspend fun getCustomer(): Response<CustomerModel>

    @Headers(
        "Authorization: Bearer ${Utils.SECRET_KEY}",
        "Stripe-Version: 2025-02-24.acacia"
    )
    @POST("v1/ephemeral_keys")
    suspend fun getEphemeralKey(
        @Query("customer") customer: String
    ): Response<EphemeralKeyModel>

    @Headers("Authorization: Bearer ${Utils.SECRET_KEY}")
    @POST("v1/payment_intents")
    suspend fun getPaymentIntent(
        @Query("customer") customer: String,
        @Query("amount") amount: String = "1000",
        @Query("currency") currency: String = "usd",
        @Query("automatic_payment_methods[enabled]") automatePay: Boolean = true
    ): Response<PaymentIntentModel>
}
