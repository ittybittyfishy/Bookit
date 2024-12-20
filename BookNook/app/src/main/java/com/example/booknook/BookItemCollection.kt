package com.example.booknook

//Stores information from the books saved in database
// Olivia Fishbough
class BookItemCollection (val title: String,
                          val authors: List<String>,
                          val imageLink: String,
                          var pages: Int,
                          var tags: List<String> = emptyList(),
                          val genres: List<String>)  // Veronica Nguyen
{
}