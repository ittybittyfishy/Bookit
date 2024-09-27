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
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.View
import android.widget.TextView
import com.example.booknook.BookItem
import com.example.booknook.BookResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), BookAdapter.RecyclerViewEvent {

    // Initialize fragments for different sections of the app
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

    // Unique API key
    private val apiKey = "AIzaSyAo2eoLcmBI9kYmd-MRCF8gqMY44gDK0uM"

    // Declare the book list and adapter for sorting functionality
    private var bookList: MutableList<BookItem> = mutableListOf()
    private lateinit var bookAdapter: BookAdapter
    private lateinit var recyclerView: RecyclerView

    // Method called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Set your activity's layout

        // Initialize RecyclerView and Adapter (with listener parameter)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)  // Set the layout manager
        bookAdapter = BookAdapter(bookList, this)  // Pass 'this' as the listener
        recyclerView.adapter = bookAdapter  // Set the adapter


        // Get references to the BottomNavigationView and banner TextView from the layout
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val bannerTextView: TextView = findViewById(R.id.bannerTextView)

        // Check if it's the user's first login
        val isFirstLogin = intent.getBooleanExtra("isFirstLogin", false)
        if (isFirstLogin) {
            // If first login, show Genre Preference Fragment for selecting genres
            replaceFragment(genrePreferenceFragment, "Select Genres")
        } else {
            // Otherwise, show the Home Fragment
            replaceFragment(homeFragment, "Home")
        }

        // Set up the BottomNavigationView's item selection listener
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                // When 'Home' is selected, load Home Fragment
                R.id.home -> replaceFragment(homeFragment, "Home")
                // When 'Collections' is selected, load Collection Fragment
                R.id.collections -> replaceFragment(collectionFragment, "My Books")
                // When 'Search' is selected, load Search Fragment
                R.id.search -> replaceFragment(searchFragment, "Search")
                // When 'Profile' is selected, load Profile Fragment
                R.id.profile -> replaceFragment(profileFragment, "Profile")
                // When 'More' is selected, show the additional options in a popup menu
                R.id.more -> showMorePopupMenu(findViewById(R.id.more))
            }
            true
        }
    }

    // Method to replace the current fragment with a new one and update the banner title
    fun replaceFragment(fragment: Fragment, title: String) {
        val transaction = supportFragmentManager.beginTransaction()
        // Replace the fragment container with the selected fragment
        transaction.replace(R.id.menu_container, fragment)
        transaction.commit()
        // Update the banner TextView with the new title
        findViewById<TextView>(R.id.bannerTextView).text = title
    }

    // Method to show a popup menu when 'More' is selected
    private fun showMorePopupMenu(view: View) {
        val popupMenu = PopupMenu(this@MainActivity, view)
        popupMenu.inflate(R.menu.more_menu)

        // Set up the item click listener for the popup menu
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.friends -> replaceFragment(friendsFragment, "Friends")
                R.id.groups -> replaceFragment(groupsFragment, "Groups")
                R.id.achievements -> replaceFragment(achievementsFragment, "Achievements")
                R.id.settings -> replaceFragment(settingsFragment, "Settings")
            }
            true
        }
        // Show the popup menu
        popupMenu.show()
    }

    // Function to search for books using an API call
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

    // Function to sort books based on different criteria
    fun sortBooks(criteria: String) {
        when (criteria) {
            "high_rating" -> {
                // Sort by rating from high to low
                bookList.sortByDescending { it.volumeInfo.averageRating ?: 0f }
            }
            "low_rating" -> {
                // Sort by rating from low to high
                bookList.sortBy { it.volumeInfo.averageRating ?: 0f }
            }
            "title_az" -> {
                // Sort by title A to Z
                bookList.sortBy { it.volumeInfo.title }
            }
            "title_za" -> {
                // Sort by title Z to A
                bookList.sortByDescending { it.volumeInfo.title }
            }
            "author" -> {
                // Sort by first author's name A to Z (if authors exist)
                bookList.sortBy { it.volumeInfo.authors?.firstOrNull() ?: "" }
            }
        }
        // Notify the adapter that the data has changed after sorting
        bookAdapter.notifyDataSetChanged()
    }

    // When the app comes back to the foreground
    override fun onResume() {
        super.onResume()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val userId = firebaseUser.uid
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("isOnline", true)  // Mark user as online
        }
    }

    // When activity is no longer in foreground, but still visible in multi-window mode
    override fun onPause() {
        super.onPause()
        setUserOffline()
    }

    // When newly launched activity covers the entire screen
    override fun onStop() {
        super.onStop()
        setUserOffline()
    }

    // When user clears out the app
    override fun onDestroy() {
        super.onDestroy()
        setUserOffline()
    }

    // Function to update the user's status to offline in database under "isOnline" field
    private fun setUserOffline() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val userId = firebaseUser.uid
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("isOnline", false)
        }
    }

    // Implementing the required method for BookAdapter.RecyclerViewEvent interface
    override fun onItemClick(position: Int) {
        val bookItem = bookList[position]
        val bookDetailsFragment = BookDetailsFragment()
        val bundle = Bundle()

        bundle.putString("bookTitle", bookItem.volumeInfo.title)
        bundle.putString("bookAuthor", bookItem.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author")
        bundle.putString("bookImage", bookItem.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"))
        bundle.putFloat("bookRating", bookItem.volumeInfo.averageRating ?: 0f)

        bookDetailsFragment.arguments = bundle
        replaceFragment(bookDetailsFragment, bookItem.volumeInfo.title)
    }
}
