package com.example.booknook

class CollectionCustomItem (val collectionName: String,
                            var books: List<BookItemCollection> = listOf(),
                            val summary: String)
{}