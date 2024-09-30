// File: MainActivity.kt
package com.example.booknook

import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.api.RetrofitInstance
import com.example.booknook.fragments.*
import com.example.booknook.utils.GenreUtils // Import GenreUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), BookAdapter.RecyclerViewEvent {

    private val homeFragment = HomeFragment()
    private val profileFragment = ProfileFragment()
    private val collectionFragment = CollectionFragment()
    private val searchFragment = SearchFragment()
    private val friendsFragment = FriendsFragment()
    private val groupsFragment = GroupsFragment()
    private val achievementsFragment = AchievmentsFragment()
    private val settingsFragment = SettingsFragment()
    private val genrePreferenceFragment = GenrePreferenceFragment()
    private val accountFragment = AccountFragment()

    private val apiKey = "AIzaSyAo2eoLcmBI9kYmd-MRCF8gqMY44gDK0uM"

    private var bookList: MutableList<BookItem> = mutableListOf()
    private lateinit var bookAdapter: BookAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize RecyclerView and Adapter if needed
        // (If you're not using RecyclerView in MainActivity, you can remove these lines)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        bookAdapter = BookAdapter(bookList, this)
        recyclerView.adapter = bookAdapter

        supportFragmentManager.setFragmentResultListener("requestKey", this) { requestKey, bundle ->
            val selectedGenres = bundle.getStringArrayList("selectedGenres")
            val languageFilter = bundle.getString("languageFilter")
            val minRating = bundle.getFloat("minRating", 0f)
            val maxRating = bundle.getFloat("maxRating", 5f)

            searchBooksWithFilters(selectedGenres, languageFilter, minRating, maxRating)
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val bannerTextView: TextView = findViewById(R.id.bannerTextView)

        val isFirstLogin = intent.getBooleanExtra("isFirstLogin", false)
        if (isFirstLogin) {
            replaceFragment(genrePreferenceFragment, "Select Genres")
        } else {
            replaceFragment(homeFragment, "Home")
        }

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(homeFragment, "Home")
                R.id.collections -> replaceFragment(collectionFragment, "My Books")
                R.id.search -> replaceFragment(searchFragment, "Search")
                R.id.profile -> replaceFragment(profileFragment, "Profile")
                R.id.more -> showMorePopupMenu(findViewById(R.id.more))
            }
            true
        }
    }

    fun replaceFragment(fragment: Fragment, title: String) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.menu_container, fragment)
        transaction.commit()
        findViewById<TextView>(R.id.bannerTextView).text = title
    }

    private fun showMorePopupMenu(view: View) {
        val popupMenu = PopupMenu(this@MainActivity, view)
        popupMenu.inflate(R.menu.more_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.friends -> replaceFragment(friendsFragment, "Friends")
                R.id.groups -> replaceFragment(groupsFragment, "Groups")
                R.id.achievements -> replaceFragment(achievementsFragment, "Achievements")
                R.id.settings -> replaceFragment(settingsFragment, "Settings")
            }
            true
        }
        popupMenu.show()
    }

    fun searchBooks(query: String, startIndex: Int, languageFilter: String? = null, callback: (List<BookItem>?) -> Unit) {
        val call = RetrofitInstance.api.searchBooks(query, startIndex, apiKey, languageFilter)
        call.enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                if (response.isSuccessful) {
                    callback(response.body()?.items)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                t.printStackTrace()
                callback(null)
            }
        })
    }

    // Updated fetchGenresForQuery function with GenreUtils normalization
    fun fetchGenresForQuery(query: String, callback: (Set<String>?) -> Unit) {
        val availableGenres = mutableSetOf<String>()
        var booksFetched = 0
        var startIndexForGenres = 0
        val totalBooksToFetch = 50

        fun fetchNextBatch() {
            searchBooks(query, startIndexForGenres, languageFilter = null) { books ->
                if (books != null && books.isNotEmpty()) {
                    books.forEach { bookItem ->
                        val genres = bookItem.volumeInfo.categories?.flatMap { category ->
                            // Normalize each genre using GenreUtils
                            category.split("/", "&").map { GenreUtils.normalizeGenre(it) }
                        } ?: emptyList()
                        availableGenres.addAll(genres)
                    }
                    booksFetched += books.size
                    startIndexForGenres += books.size
                    if (booksFetched < totalBooksToFetch && books.size > 0) {
                        fetchNextBatch()
                    } else {
                        callback(availableGenres)
                    }
                } else {
                    callback(availableGenres)
                }
            }
        }

        fetchNextBatch()
    }

    // Updated searchBooksWithFilters with GenreUtils normalization
    fun searchBooksWithFilters(genres: List<String>?, language: String?, minRating: Float, maxRating: Float) {
        val query = "some search query" // You might want to pass the actual query here

        searchBooks(query, 0, language) { bookItems ->
            val filteredBooks = bookItems?.filter { book ->
                val bookGenres = book.volumeInfo.categories?.flatMap { category ->
                    // Normalize each genre using GenreUtils
                    category.split("/", "&").map { GenreUtils.normalizeGenre(it) }
                } ?: emptyList()
                val rating = book.volumeInfo.averageRating ?: 0f

                // Normalize input genres for comparison
                val normalizedInputGenres = genres?.map { GenreUtils.normalizeGenre(it) } ?: emptyList()

                val genreMatch = normalizedInputGenres.isEmpty() || normalizedInputGenres.any { genre ->
                    bookGenres.contains(genre)
                }

                val ratingInRange = rating in minRating..maxRating

                genreMatch && ratingInRange
            }

            bookList.clear()
            filteredBooks?.let { bookList.addAll(it) }
            bookAdapter.notifyDataSetChanged()

            // Optionally, handle the case when no books match the filters
            if (filteredBooks.isNullOrEmpty()) {
                Toast.makeText(this, "No books found matching the filters.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sortBooks(criteria: String) {
        when (criteria) {
            "high_rating" -> bookList.sortByDescending { it.volumeInfo.averageRating ?: 0f }
            "low_rating" -> bookList.sortBy { it.volumeInfo.averageRating ?: 0f }
            "title_az" -> bookList.sortBy { it.volumeInfo.title ?: "" }
            "title_za" -> bookList.sortByDescending { it.volumeInfo.title ?: "" }
            "author" -> bookList.sortBy { it.volumeInfo.authors?.firstOrNull() ?: "" }
        }
        bookAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser?.uid?.let { userId ->
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("isOnline", true)
        }
    }

    override fun onPause() {
        super.onPause()
        setUserOffline()
    }

    override fun onStop() {
        super.onStop()
        setUserOffline()
    }

    override fun onDestroy() {
        super.onDestroy()
        setUserOffline()
    }

    private fun setUserOffline() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser?.uid?.let { userId ->
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("isOnline", false)
        }
    }

    override fun onItemClick(position: Int) {
        val bookItem = bookList[position]
        val bookDetailsFragment = BookDetailsFragment()
        val bundle = Bundle()

        bundle.putString("bookTitle", bookItem.volumeInfo.title)
        bundle.putString("bookAuthor", bookItem.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author")
        bundle.putString(
            "bookImage",
            bookItem.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
        )
        bundle.putFloat("bookRating", bookItem.volumeInfo.averageRating ?: 0f)

        bookDetailsFragment.arguments = bundle
        replaceFragment(bookDetailsFragment, bookItem.volumeInfo.title)
    }
}
