package com.example.booknook

import android.os.Bundle
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.booknook.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.View
import android.widget.TextView
import com.example.booknook.api.GoogleBooksApi
import com.example.booknook.BookItem

class MainActivity : AppCompatActivity() {
    private val homeFragment = HomeFragment()
    private val profileFragment = ProfileFragment()
    private val collectionFragment = CollectionFragment()
    private val searchFragment = SearchFragment()
    private val friendsFragment = FriendsFragment()
    private val groupsFragment = GroupsFragment()
    private val achievementsFragment = AchievmentsFragment()
    private val settingsFragment = SettingsFragment()

    private lateinit var googleBooksApi: GoogleBooksApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        googleBooksApi = GoogleBooksApi(this)

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

    fun searchBooks(query: String, callback: (List<BookItem>?) -> Unit) {
        googleBooksApi.searchBooks(query, callback)
    }
}
