package com.example.booknook.api

import com.example.booknook.BookResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

//this is an abstract class that defines methods but not the actual implementation
interface GoogleBooksApiService {
    @GET("volumes")
    fun searchBooks(
        @Query("q") query: String,
        @Query("startIndex") startIndex: Int,
        @Query("key") apiKey: String,
        @Query("langRestrict") language: String? = null // This must be a separate query parameter
    ): Call<BookResponse>
}

