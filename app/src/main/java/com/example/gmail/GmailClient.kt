package com.example.gmail

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/** Supplies a fresh access token per request. Implemented by the auth layer. */
fun interface TokenProvider {
    fun accessToken(): String
}

object GmailClient {
    fun create(tokens: TokenProvider, baseUrl: String = "https://gmail.googleapis.com/"): GmailApi {
        val auth = Interceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("Authorization", "Bearer ${tokens.accessToken()}")
                    .build()
            )
        }
        val http = OkHttpClient.Builder().addInterceptor(auth).build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(http)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GmailApi::class.java)
    }
}
