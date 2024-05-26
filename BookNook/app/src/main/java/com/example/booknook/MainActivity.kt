// MainActivity.kt
package com.example.booknook

import android.os.Bundle
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.booknook.fragments.CollectionFragment
import com.example.booknook.fragments.FriendsFragment
import com.example.booknook.fragments.GroupsFragment
import com.example.booknook.fragments.HomeFragment
import com.example.booknook.fragments.ProfileFragment
import com.example.booknook.fragments.SearchFragment
import com.example.booknook.fragments.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.View
import com.example.booknook.fragments.AchievmentsFragment

//testing comment
class MainActivity : AppCompatActivity() {
    private val homeFragment = HomeFragment()
    private val profileFragment = ProfileFragment()
    private val collectionFragment = CollectionFragment()
    private val searchFragment = SearchFragment()
    private val friendsFragment = FriendsFragment()
    private val groupsFragment = GroupsFragment()
    private val achievementsFragment = AchievmentsFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        replaceFragment(homeFragment)

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(homeFragment)
                R.id.collections -> replaceFragment(collectionFragment)
                R.id.search -> replaceFragment(searchFragment)
                R.id.profile -> replaceFragment(profileFragment)
                R.id.more -> showMorePopupMenu(findViewById(R.id.more))
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
    private fun showMorePopupMenu(view: View) {
        val popupMenu = PopupMenu(this@MainActivity, view)
        popupMenu.inflate(R.menu.more_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.friends -> replaceFragment(friendsFragment)
                R.id.groups -> replaceFragment(groupsFragment)
                R.id.achievments -> replaceFragment(achievementsFragment)
                R.id.settings -> replaceFragment(settingsFragment)
            }
            true
        }
        popupMenu.show()
    }
}