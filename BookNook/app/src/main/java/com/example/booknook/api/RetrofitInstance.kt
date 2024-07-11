package com.example.booknook.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//object named retrofitinstance. There can only be one instance of this object
object RetrofitInstance {
    //we declare our google api url as a constant
    private const val BASE_URL = "https://www.googleapis.com/books/v1/"

    //val parameter values cannot be changed, read only
    //here we create a parameter called api of the "googleBookApiService" type
    //lazy means that it will be initialized when first accessed vs when the program starts
    //this function provides a ready to use API service
    val api: GoogleBooksApiService by lazy {
        Retrofit.Builder() //retrofit instance
            .baseUrl(BASE_URL) //setting the url request to the google api
            .addConverterFactory(GsonConverterFactory.create()) //tells retrofit how to convert JSON data to kotlin
            .build() //builds the retrofit instance
            .create(GoogleBooksApiService::class.java) //creates implementation of the api to create api calls
    }
}
