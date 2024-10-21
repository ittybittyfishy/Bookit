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
import com.example.booknook.utils.GenreUtils
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
    private val achievementsFragment = AchievementsFragment()
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

        // Olivia Fishbough
        // Initialize UI elements for bottom navigation and banner text
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val bannerTextView: TextView = findViewById(R.id.bannerTextView)

        // Itzel Medina
        val isFirstLogin = intent.getBooleanExtra("isFirstLogin", false)
        if (isFirstLogin) {
            replaceFragment(genrePreferenceFragment, "Select Genres")
        } else {
            replaceFragment(homeFragment, "Home")
        }

        // Olivia Fishbough
        // Set up the bottom navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(homeFragment, "Home") // Navigate to Home fragment
                R.id.collections -> replaceFragment(collectionFragment, "My Books") // Navigate to My Books (collections) fragment
                // Itzel Medina
                R.id.search -> {
                    val bundle = Bundle()
                    bundle.putBoolean("clearSearch", true)
                    searchFragment.arguments = bundle
                    replaceFragment(searchFragment, "Search")
                }
                R.id.profile -> replaceFragment(profileFragment, "Profile") // Navigate to Profile fragment
                R.id.more -> showMorePopupMenu(findViewById(R.id.more)) // Show the More options popup menu
            }
            true // Return true to indicate the event has been handled
        }
    }

    // Olivia Fishbough
    // Function to replace the current fragment with the new one and update the banner title
    fun replaceFragment(fragment: Fragment, title: String) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.menu_container, fragment) // Replace the fragment in the specified container
        transaction.commit() // Commit the transaction
        findViewById<TextView>(R.id.bannerTextView).text = title
    }

    // Olivia Fishbough
    // Function to show a popup menu with additional options
    private fun showMorePopupMenu(view: View) {
        val popupMenu = PopupMenu(this@MainActivity, view) // Create a popup menu
        popupMenu.inflate(R.menu.more_menu) // Inflate the menu from XML

        // Set a listener for menu item clicks
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.friends -> replaceFragment(friendsFragment, "Friends") // Navigate to Friends fragment
                R.id.groups -> replaceFragment(groupsFragment, "Groups") // Navigate to Groups fragment
                R.id.achievements -> replaceFragment(achievementsFragment, "Achievements") // Navigate to Achievements fragment
                R.id.settings -> replaceFragment(settingsFragment, "Settings") // Navigate to Settings fragment
            }
            true // Return true to indicate the event has been handled
        }
        popupMenu.show() // Show the popup menu
    }

    fun searchBooks(query: String, startIndex: Int, languageFilter: String? = null, maxResults: Int = 40, callback: (List<BookItem>?) -> Unit) {
        // API call to search books using RetrofitInstance
        val call = RetrofitInstance.api.searchBooks(query, startIndex, maxResults, apiKey, languageFilter)

        // Enqueue the API call to run asynchronously
        call.enqueue(object : Callback<BookResponse> {
            override fun onResponse(call: Call<BookResponse>, response: Response<BookResponse>) {
                // Log the HTTP response code
                Log.d("API Response", "Response Code: ${response.code()}")

                // Log the headers from the API response
                Log.d("API Response", "Headers: ${response.headers()}")

                if (response.isSuccessful) {
                    // Log the response body (book items)
                    Log.d("API Response", "Response Body: ${response.body()}")

                    // Pass the book items to the callback
                    callback(response.body()?.items)
                } else {
                    // Log the error message in case of a failed response
                    Log.e("API Error", "Error Body: ${response.errorBody()?.string()}")
                    callback(null) // Return null if the response was not successful
                }
            }

            override fun onFailure(call: Call<BookResponse>, t: Throwable) {
                // Log the failure reason if the API call fails
                Log.e("API Failure", "Failure Message: ${t.message}")
                t.printStackTrace()
                callback(null) // Return null on failure
            }
        })
    }

    fun fetchGenresForQuery(query: String, language: String?, minRating: Float, maxRating: Float, callback: (Set<String>?) -> Unit) {
        val availableGenres = mutableSetOf<String>() // Set to hold unique genres
        var booksFetched = 0 // Counter for fetched books
        var startIndexForGenres = 0 // Tracks pagination index for fetching books
        val totalBooksToFetch = 50 // Adjust the total books to fetch based on performance needs

        // Recursive function to fetch books in batches
        fun fetchNextBatch() {
            searchBooks(query, startIndexForGenres, language) { books ->
                if (books != null && books.isNotEmpty()) {
                    // Iterate over the fetched books and filter them by rating
                    books.forEach { book ->
                        val rating = book.volumeInfo.averageRating ?: 0f
                        if (rating in minRating..maxRating) {
                            // Split categories and normalize genres before adding them to the set
                            val genres = book.volumeInfo.categories?.flatMap { category ->
                                category.split("/", "&").map { GenreUtils.normalizeGenre(it) }
                            } ?: emptyList()
                            availableGenres.addAll(genres) // Add genres to the set
                        }
                    }
                    booksFetched += books.size // Update the counter for fetched books
                    startIndexForGenres += books.size // Move to the next batch
                    if (booksFetched < totalBooksToFetch && books.isNotEmpty()) {
                        fetchNextBatch() // Continue fetching more books if needed
                    } else {
                        callback(availableGenres) // Return the available genres after all fetching is done
                    }
                } else {
                    // Return the genres even if no books are found in the batch
                    callback(availableGenres)
                }
            }
        }

        fetchNextBatch() // Start fetching the first batch
    }

    fun searchBooksWithFilters(genres: List<String>?, language: String?, minRating: Float, maxRating: Float) {
        val query = "some search query" // Modify this query as needed

        // Call searchBooks to fetch books with the given query and language filter
        searchBooks(query, 0, language) { bookItems ->
            val filteredBooks = bookItems?.filter { book ->
                // Extract and normalize genres from the book's categories
                val bookGenres = book.volumeInfo.categories?.flatMap { category ->
                    category.split("/", "&").map { GenreUtils.normalizeGenre(it) }
                } ?: emptyList()

                // Get the book's rating or set it to 0 if not available
                val rating = book.volumeInfo.averageRating ?: 0f

                // Normalize the input genres to match against the book's genres
                val normalizedInputGenres = genres?.map { GenreUtils.normalizeGenre(it) } ?: emptyList()

                // Determine if the book's genres match the input genres (if any)
                val genreMatch = normalizedInputGenres.isEmpty() || normalizedInputGenres.any { genre ->
                    bookGenres.contains(genre)
                }

                // Check if the book's rating is within the specified range
                val ratingInRange = rating in minRating..maxRating

                // Return true if both genre match and rating range conditions are satisfied
                genreMatch && ratingInRange
            }

            // Clear the current book list and add the filtered books
            bookList.clear()
            filteredBooks?.let { bookList.addAll(it) }
            bookAdapter.notifyDataSetChanged() // Notify the adapter of data changes

            // Show a message if no books matched the filters
            if (filteredBooks.isNullOrEmpty()) {
                Toast.makeText(this, "No books found matching the filters.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sortBooks(criteria: String) {
        // Sort the book list based on the specified criteria
        when (criteria) {
            "high_rating" -> bookList.sortByDescending { it.volumeInfo.averageRating ?: 0f } // Sort by highest rating
            "low_rating" -> bookList.sortBy { it.volumeInfo.averageRating ?: 0f } // Sort by lowest rating
            "title_az" -> bookList.sortBy { it.volumeInfo.title ?: "" } // Sort by title A-Z
            "title_za" -> bookList.sortByDescending { it.volumeInfo.title ?: "" } // Sort by title Z-A
            "author" -> bookList.sortBy { it.volumeInfo.authors?.firstOrNull() ?: "" } // Sort by author's name
        }
        bookAdapter.notifyDataSetChanged() // Notify the adapter of data changes after sorting
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
