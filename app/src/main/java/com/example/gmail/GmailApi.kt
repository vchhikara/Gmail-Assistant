package com.example.gmail

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Gmail REST v1, "me" user. Authorization is added by [GmailClient]. */
interface GmailApi {
    @GET("gmail/v1/users/me/profile")
    suspend fun getProfile(): Profile

    @GET("gmail/v1/users/me/messages")
    suspend fun listMessages(
        @Query("labelIds") labelIds: String = "INBOX",
        @Query("maxResults") maxResults: Int = 100,
        @Query("pageToken") pageToken: String? = null
    ): MessageListResponse

    @GET("gmail/v1/users/me/messages/{id}")
    suspend fun getMessage(
        @Path("id") id: String,
        @Query("format") format: String = "metadata",
        @Query("metadataHeaders") metadataHeaders: List<String> = METADATA_HEADERS
    ): GmailMessage

    @GET("gmail/v1/users/me/history")
    suspend fun listHistory(
        @Query("startHistoryId") startHistoryId: String,
        @Query("labelId") labelId: String = "INBOX",
        @Query("maxResults") maxResults: Int = 500,
        @Query("pageToken") pageToken: String? = null
    ): HistoryResponse

    @POST("gmail/v1/users/me/messages/batchModify")
    suspend fun batchModify(@Body body: BatchModifyRequest)

    @POST("gmail/v1/users/me/messages/{id}/trash")
    suspend fun trash(@Path("id") id: String): GmailMessage

    companion object {
        val METADATA_HEADERS = listOf("From", "Subject", "Date", "List-Unsubscribe", "List-Id", "Precedence")
    }
}
