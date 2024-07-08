package com.example.booknook

//used to model a response from the API that returns the list of books
//each response contains property items which is a list of bookitem objects
data class BookResponse(val items: List<BookItem>)

