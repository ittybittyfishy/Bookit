package com.example.booknook.api

import com.example.booknook.BookResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApiService {
    @GET("volumes")
    fun searchBooks(
        @Query("q") query: String,
        @Query("startIndex") startIndex: Int,
        @Query("key") apiKey: String
    ): Call<BookResponse>
}
