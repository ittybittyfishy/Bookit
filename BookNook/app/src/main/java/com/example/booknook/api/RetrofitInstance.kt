package com.example.booknook.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// We define an object called RetrofitInstance.
// In Kotlin, an object is a singleton, meaning only one instance of it will exist throughout the program.
// It's used here to make sure there's only one Retrofit instance managing the API requests.
object RetrofitInstance {

    // This is a constant that stores the base URL of the Google Books API.
    // By declaring it with `const`, it's immutable (can't be changed) and is a compile-time constant.
    private const val BASE_URL = "https://www.googleapis.com/books/v1/"

    // Here we declare a property `api` of type `GoogleBooksApiService`.
    // The `by lazy` keyword is used to delay the initialization until it's actually needed.
    // This saves resources because it won't create the instance until the first time it's accessed.
    val api: GoogleBooksApiService by lazy {

        // We create a Retrofit instance using the builder pattern.
        // Retrofit is a library that simplifies the process of making network requests to a REST API.
        Retrofit.Builder()
            // `baseUrl` sets the base URL for the API. This is where all requests will be sent.
            // The path we define in the service (like "volumes") will be appended to this URL.
            .baseUrl(BASE_URL)

            // `addConverterFactory` tells Retrofit how to convert the data it receives.
            // GsonConverterFactory is used here, which converts JSON data from the API into Kotlin objects automatically.
            .addConverterFactory(GsonConverterFactory.create())

            // `build()` completes the construction of the Retrofit instance.
            .build()

            // `create()` tells Retrofit to create an implementation of the `GoogleBooksApiService` interface.
            // This allows us to make network requests using the methods defined in that interface.
            .create(GoogleBooksApiService::class.java)
    }
}
