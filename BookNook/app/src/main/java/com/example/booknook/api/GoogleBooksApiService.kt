package com.example.booknook.api

import com.example.booknook.BookResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// This interface defines methods for making network requests to the Google Books API.
// Retrofit will generate the actual implementation behind the scenes, so you don't have to.

interface GoogleBooksApiService {

    // This annotation tells Retrofit that this method corresponds to a GET request
    // The "volumes" part is the endpoint of the Google Books API (e.g., "https://www.googleapis.com/books/v1/volumes")
    @GET("volumes")
    fun searchBooks(
        // The @Query annotation allows us to pass parameters to the request URL, such as the search query.
        // For example, if we call searchBooks("Harry Potter"), Retrofit will send a request like:
        // https://www.googleapis.com/books/v1/volumes?q=Harry+Potter
        @Query("q") query: String, // 'query' is the search term for books, like a title or author's name.

        // This parameter is used for pagination, indicating where to start fetching results from.
        @Query("startIndex") startIndex: Int, // Start index for paginated results.

        //sets the max results shown when search queries are performed
        @Query("maxResults") maxResults: Int = 40,

        // The 'key' parameter is the API key, which is needed to authenticate your app with Google Books API.
        @Query("key") apiKey: String, // Your API key to authenticate requests.

        // The 'langRestrict' parameter allows you to filter books by language.
        // If provided, it will restrict results to the specified language (e.g., 'en' for English, 'fr' for French).
        // It's optional, meaning you can call this method without passing it if you don't want to restrict results by language.
        @Query("langRestrict") language: String? = null // Optional parameter to filter by language.
    ): Call<BookResponse> // The return type is Call<BookResponse>, which represents the HTTP response.
    // BookResponse is a data model class that holds the structure of the response we expect from the API.

    // Yunjong Noh
    // Using Retrofit library, networking with Google books API, return api request
    // Base URL for Google Books API
    companion object {
        private const val BASE_URL = "https://www.googleapis.com/books/v1/"

        // Creates and returns an instance of GoogleBooksApiService using Retrofit
        fun create(): GoogleBooksApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL) // Set the API base URL
                // Convert JSON response to Kotlin objects using Gson(Gson is convert json as respective google's library)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(GoogleBooksApiService::class.java) // Return an API service instance
        }
    }
}