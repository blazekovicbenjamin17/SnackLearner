
package com.example.snacklearner

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        if (savedInstanceState == null) {
            loadLoginFragment()
        }

        checkUserRole()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                    } else {
                        finish()
                    }
                }
            }
        })
    }

    fun getToolbar(): Toolbar = toolbar
    fun getDrawerLayout(): DrawerLayout = drawerLayout

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> loadSearchFragment()
            R.id.nav_saved -> loadSavedRecipesFragment()
            R.id.nav_add_recipe -> loadAddRecipeFragment()
            R.id.nav_settings -> loadSettingsFragment()
            R.id.admin_settings -> checkAdminAndLoad()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun checkAdminAndLoad() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    val role = doc.getString("role") ?: "user"
                    if (role.equals("admin", true)) {
                        loadAdminFragment()
                    } else {
                        Toast.makeText(this, "Nemate prava za pristup Admin panelu", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Greška prilikom provjere uloge", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Niste prijavljeni", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLoginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, LoginFragment())
            .commit()
    }

    fun loadSearchFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, SearchFragment())
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

    private fun loadSettingsFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, SettingsFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun loadAdminFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, AdminFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun checkUserRole() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val menu = navigationView.menu
        val adminItem = menu.findItem(R.id.admin_settings)
        adminItem?.isVisible = false

        currentUser?.let { user ->
            FirebaseFirestore.getInstance().collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    if (role.equals("admin", ignoreCase = true)) {
                        runOnUiThread { adminItem?.isVisible = true }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Greška pri dohvatu role: ${e.message}")
                }
        }
    }
}
