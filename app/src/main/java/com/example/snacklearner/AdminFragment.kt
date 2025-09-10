package com.example.snacklearner

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var buttonUsers: MaterialButton
    private lateinit var buttonRecipes: MaterialButton

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var isAdmin = false

    private var usersAdapter: UserAdapter? = null
    private var recipesAdapter: AdminRecipeAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_admin, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.usersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchEditText = view.findViewById(R.id.searchEditText)
        toggleGroup = view.findViewById(R.id.toggleGroup)
        buttonUsers = view.findViewById(R.id.buttonUsers)
        buttonRecipes = view.findViewById(R.id.buttonRecipes)

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Niste prijavljeni.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "user"
                isAdmin = role == "admin"
                if (!isAdmin) {
                    Toast.makeText(requireContext(), "Niste admin.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                setupAdapters()
                setupToggleGroup()
                setupSearch()
                loadUsers()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška prilikom provjere admina.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupAdapters() {
        usersAdapter = UserAdapter(emptyList()) { uid -> deleteUser(uid) }
        recipesAdapter = AdminRecipeAdapter(emptyList()) { id -> deleteRecipe(id) }
    }

    private fun setupToggleGroup() {
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.buttonUsers -> loadUsers()
                    R.id.buttonRecipes -> loadRecipes()
                }
            }
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (buttonUsers.isChecked) usersAdapter?.filterData(query)
                else if (buttonRecipes.isChecked) recipesAdapter?.filterData(query)
            }
        })
    }

    private fun loadUsers() {
        firestore.collection("users").get()
            .addOnSuccessListener { result ->
                val users = result.documents.map { doc ->
                    val email = doc.getString("email") ?: ""
                    val role = doc.getString("role") ?: "user"
                    Triple(email, role, doc.id)
                }.filter { (_, role, _) -> role != "admin" }
                usersAdapter?.updateData(users)
                usersAdapter?.setAdminMode(isAdmin)
                recyclerView.adapter = usersAdapter
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška prilikom učitavanja korisnika.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadRecipes() {
        firestore.collection("recipes").get()
            .addOnSuccessListener { result ->
                val recipes = result.documents.map { doc ->
                    val id = doc.id
                    val title = doc.getString("title") ?: ""
                    val description = doc.getString("description") ?: ""
                    val username = doc.getString("username") ?: ""
                    val ingredients = doc.getString("ingredients") ?: ""
                    AdminRecipe(id, title, description, username, ingredients)
                }
                recipesAdapter?.updateData(recipes)
                recyclerView.adapter = recipesAdapter
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška prilikom učitavanja recepata.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteUser(uid: String) {
        firestore.collection("users").document(uid).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Korisnik obrisan.", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteRecipe(id: String) {
        firestore.collection("recipes").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Recept obrisan.", Toast.LENGTH_SHORT).show()
                loadRecipes()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
