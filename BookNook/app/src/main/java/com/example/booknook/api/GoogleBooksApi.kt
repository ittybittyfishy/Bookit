package com.example.booknook.api

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.booknook.BookItem

class GoogleBooksApi(private val context: Context) {
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)
    private val gson = Gson()

    fun searchBooks(query: String, callback: (List<BookItem>?) -> Unit) {
        val url = "https://www.googleapis.com/books/v1/volumes?q=$query"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                val itemsArray = response.optJSONArray("items")
                if (itemsArray != null) {
                    val itemType = object : TypeToken<List<BookItem>>() {}.type
                    val books: List<BookItem> = gson.fromJson(itemsArray.toString(), itemType)
                    callback(books)
                } else {
                    callback(null)
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                callback(null)
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}