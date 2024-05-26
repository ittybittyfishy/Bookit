// MainActivity.kt
package com.example.booknook

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.booknook.fragments.CollectionFragment
import com.example.booknook.fragments.HomeFragment
import com.example.booknook.fragments.MoreFragment
import com.example.booknook.fragments.ProfileFragment
import com.example.booknook.fragments.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

//testing comment
class MainActivity : AppCompatActivity() {
    private val homeFragment = HomeFragment()
    private val profileFragment = ProfileFragment()
    private val collectionFragment = CollectionFragment()
    private val searchFragment = SearchFragment()
    private val moreFragment = MoreFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        replaceFragment(homeFragment)

        bottomNavigationView.setOnItemSelectedListener{
            when (it.itemId)
            {
                R.id.home -> replaceFragment(homeFragment)
                R.id.collections ->  replaceFragment(collectionFragment)
                R.id.search -> replaceFragment(searchFragment)
                R.id.profile -> replaceFragment(profileFragment)
                R.id.more ->  replaceFragment(moreFragment)
                }
            true
            }
        }

    private fun replaceFragment(fragment: Fragment)
    {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.menu_container, fragment)
            transaction.commit()
    }
}
