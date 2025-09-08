package com.example.snacklearner

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Firebase.analytics
        Log.d("FirebaseTest", "Firebase se uspješno inicijalizirao!")

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = "Zdravi recepti"

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        if (savedInstanceState == null) {
            loadLoginFragment()
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> loadSearchFragment()
            R.id.nav_saved -> loadSavedRecipesFragment()
            R.id.nav_add_recipe -> loadAddRecipeFragment()
            R.id.nav_settings -> loadSettingsFragment()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadLoginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, LoginFragment())
            .commit()
    }

    private fun loadSearchFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, SearchFragment())
            .commit()
    }

    private fun loadSettingsFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, SettingsFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun loadSavedRecipesFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, SavedRecipesFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun loadAddRecipeFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, AddRecipeFragment())
            .addToBackStack(null)
            .commit()
    }

    // NOVA METODA za prilagodbu menija ovisno o roli:
    fun updateDrawerForRole(role: String) {
        val menu = navigationView.menu

        // Ako je admin
        if (role == "admin") {
            menu.findItem(R.id.nav_home)?.isVisible = true
            menu.findItem(R.id.nav_settings)?.isVisible = true
            menu.findItem(R.id.nav_saved)?.isVisible = true
            menu.findItem(R.id.nav_add_recipe)?.isVisible = true
        } else {
            // Ako je običan korisnik
            menu.findItem(R.id.nav_home)?.isVisible = true
            menu.findItem(R.id.nav_settings)?.isVisible = true
            menu.findItem(R.id.nav_saved)?.isVisible = true
            // Sakrij dodavanje recepta ako nije admin
            menu.findItem(R.id.nav_add_recipe)?.isVisible = true
        }
    }

    fun getDrawerLayout(): DrawerLayout = drawerLayout
    fun getToolbar(): Toolbar = toolbar
}
