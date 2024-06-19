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
import com.example.booknook.model.BookItem
import com.example.booknook.model.BookResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//this is the updated version that uses Retrofit

class MainActivity : AppCompatActivity() {
    private val homeFragment = HomeFragment()
    private val profileFragment = ProfileFragment()
    private val collectionFragment = CollectionFragment()
    private val searchFragment = SearchFragment()
    private val friendsFragment = FriendsFragment()
    private val groupsFragment = GroupsFragment()
    private val achievementsFragment = AchievmentsFragment()
    private val settingsFragment = SettingsFragment()

    private val apiKey = "AIzaSyAo2eoLcmBI9kYmd-MRCF8gqMY44gDK0uM" // Replace with your actual API key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val bannerTextView: TextView = findViewById(R.id.bannerTextView)

        replaceFragment(homeFragment, "Home")

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(homeFragment, "Home")
                R.id.collections -> replaceFragment(collectionFragment, "Collections")
                R.id.search -> replaceFragment(searchFragment, "Search")
                R.id.profile -> replaceFragment(profileFragment, "Profile")
                R.id.more -> showMorePopupMenu(findViewById(R.id.more))
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
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
                R.id.achievments -> replaceFragment(achievementsFragment, "Achievements")
                R.id.settings -> replaceFragment(settingsFragment, "Settings")
            }
            true
        }
        popupMenu.show()
    }

    fun searchBooks(query: String, startIndex: Int, callback: (List<BookItem>?) -> Unit) {
        val call = RetrofitInstance.api.searchBooks(query, startIndex, apiKey)
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
}
