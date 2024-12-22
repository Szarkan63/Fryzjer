package com.example.fryzjer.data.model
import com.example.fryzjer.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.serialization.Serializable


@Serializable
data class ReservationInput(
    val reservation_id: String,
    val date: String,
    val description: String?,
    val time: String?,
    val is_accepted: Boolean?,
    val user_id: String,
)

object ReservationRepository {

    suspend fun createReservation(
        reservation_id: String,
        date: String,
        description: String?,
        time: String?,
        is_accepted: Boolean?,
        user_id: String
    ): PostgrestResult {
        // Create ReservationInput object
        val reservation = ReservationInput(reservation_id, date, description, time, is_accepted,user_id)

        // Insert the reservation into Supabase using SupabaseClient
        return SupabaseClient.supabase.from("Reservations").insert(reservation)
    }
    suspend fun getReservationsByUserId(userId: String): PostgrestResult {
        // Retrieve reservations for the specified user ID
        return SupabaseClient.supabase
            .from("Reservations")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
    }
}


