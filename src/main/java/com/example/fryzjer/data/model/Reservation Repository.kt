package com.example.fryzjer.data.model
import android.util.Log
import com.example.fryzjer.data.network.SupabaseClient
import com.example.fryzjer.data.network.SupabaseClient.client
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.query.Columns
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
        val reservation =
            ReservationInput(reservation_id, date, description, time, is_accepted, user_id)

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

    suspend fun getAllReservations(): PostgrestResult {
        // Retrieve all reservations from the Reservations table
        return SupabaseClient.supabase
            .from("Reservations")
            .select() // Selecting all columns from the Reservations table
    }

    suspend fun updateReservationStatus(
        reservationId: String,
        isAccepted: Boolean
    ) {
        try {
            SupabaseClient.supabase
                .from("Reservations")
                .update(
                    {
                        set("is_accepted", isAccepted)
                    }
                ) {
                    filter {
                        eq("reservation_id", reservationId)
                    }
                }
            Log.d("ReservationRepository", "Successfully updated reservation $reservationId")
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error updating reservation $reservationId", e)
            throw e
        }
    }
}





