package com.example.paymentgateway

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface RentAllApiService {
    @POST("graphql")
    suspend fun getAllReservations(
        @Header("Authorization") token: String,
        @Body request: GraphQLRequest
    ): Response<ReservationResponse>
}

data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any>
)

data class ReservationResponse(
    val data: ReservationData
)

data class ReservationData(
    val getAllReservation: GetAllReservation
)

data class GetAllReservation(
    val result: List<Reservation>,
    val count: Int,
    val status: String
)

data class Reservation(
    val id: String,
    val hostId: String,
    val guestId: String,
    val checkIn: String,
    val checkOut: String,
    val hostUser: HostUser,
    val guestUser: GuestUser,
    val listData: ListData
)

data class HostUser(
    val email: String,
    val phoneNumber: String
)

data class GuestUser(
    val email: String,
    val phoneNumber: String
)

data class ListData(
    val title: String,
    val city: String,
    val country: String
)
