package com.example.booknook.api

import com.example.booknook.BookResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

//this is an abstract class that defines methods but not the actual implementation
interface GoogleBooksApiService {
    @GET("volumes") //make an HTTP GET request to the volume url path of the api
    //searchbooks is a function that takes in the following parameters
    fun searchBooks(
        //retrofit must include a query named q in the url. This is the search term
        @Query("q") query: String,
        //this is the starting index for the search results
        @Query("startIndex") startIndex: Int,
        //this represents the API key used to authenticate the api
        @Query("key") apiKey: String
    ): Call<BookResponse>
    //the function calls the object book response which represents the http request and response
}
