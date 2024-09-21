package com.example.booknook

import android.os.Bundle
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.booknook.api.RetrofitInstance
import com.example.booknook.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.booknook.BookItem
import com.example.booknook.BookResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//this is the updated version that uses Retrofit

class MainActivity : AppCompatActivity() {

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

    //Unique api key
    private val apiKey = "AIzaSyAo2eoLcmBI9kYmd-MRCF8gqMY44gDK0uM"

    // Method called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout resource for this activity
        setContentView(R.layout.activity_main)

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
    fun searchBooks(query: String, startIndex: Int, callback: (List<BookItem>?) -> Unit) {
        val call = RetrofitInstance.api.searchBooks(query, startIndex, apiKey)
        call.enqueue(object : Callback<BookResponse> {
            // When the API call is successfulgithub is t
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
}