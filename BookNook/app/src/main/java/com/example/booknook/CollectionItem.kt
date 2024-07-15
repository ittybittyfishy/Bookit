package com.example.booknook

class CollectionItem (val title: String,
                      val books: List<BookItem>,
                      var isExpanded: Boolean = false) {}