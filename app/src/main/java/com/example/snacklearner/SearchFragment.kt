package com.example.snacklearner

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var searchEditText: EditText
    private lateinit var adminSettingsButton: Button
    private lateinit var sortSpinner: Spinner
    private var allRecipes: List<Recipe> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recipeAdapter = RecipeAdapter(
            emptyList(),
            onLikeClicked = { id -> likeRecipe(id) },
            onDislikeClicked = { id -> dislikeRecipe(id) },
            onRecipeClicked = { recipe -> openRecipeDetails(recipe) }
        )
        recyclerView.adapter = recipeAdapter

        searchEditText = view.findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = applyFilters()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        sortSpinner = view.findViewById(R.id.sortSpinner)
        setupSortSpinner()

        adminSettingsButton = view.findViewById(R.id.admin_settings)
        adminSettingsButton.visibility = View.GONE
        checkUserRole()
        adminSettingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AdminFragment())
                .addToBackStack(null)
                .commit()
        }

        loadRecipes()
    }

    private fun checkUserRole() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.getString("role")?.equals("admin", true) == true)
                        adminSettingsButton.visibility = View.VISIBLE
                }
        }
    }

    private fun loadRecipes() {
        firestore.collection("recipes").get()
            .addOnSuccessListener { result ->
                allRecipes = result.documents.map { doc ->
                    val timestampValue = doc.get("createdAt")
                    val timestampLong = when (timestampValue) {
                        is Timestamp -> timestampValue.toDate().time
                        is Number -> timestampValue.toLong()
                        else -> 0L
                    }

                    Recipe(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        username = doc.getString("username") ?: "",
                        ingredients = (doc.get("ingredients") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        likes = (doc.getLong("likes") ?: 0L).toInt(),
                        dislikes = (doc.getLong("dislikes") ?: 0L).toInt(),
                        favorites = (doc.getLong("favorites") ?: 0L).toInt(),
                        timestamp = timestampLong
                    )
                }
                applyFilters()
            }
    }

    private fun setupSortSpinner() {
        val options = listOf(
            "Reset Filters",    // opcija za reset
            "Most Liked",
            "Most Disliked",
            "A → Z",
            "Z → A",
            "Newest → Oldest",
            "Oldest → Newest"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = adapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = options[position]
                if (selected == "Reset Filters") {
                    searchEditText.setText("")   // reset pretragu
                    applyFilters()               // primijeni bez filtera
                } else {
                    applyFilters()               // primijeni odabrano sortiranje
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun applyFilters() {
        var filtered = allRecipes
        val query = searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            filtered = filtered.filter { recipe ->
                recipe.title.contains(query, ignoreCase = true) ||
                        recipe.description.contains(query, ignoreCase = true)
            }
        }

        filtered = when (sortSpinner.selectedItem.toString()) {
            "Most Liked" -> filtered.sortedByDescending { it.likes }
            "Most Disliked" -> filtered.sortedByDescending { it.dislikes }
            "A → Z" -> filtered.sortedBy { it.title.lowercase() }
            "Z → A" -> filtered.sortedByDescending { it.title.lowercase() }
            "Newest → Oldest" -> filtered.sortedByDescending { it.timestamp }
            "Oldest → Newest" -> filtered.sortedBy { it.timestamp }
            else -> filtered
        }

        recipeAdapter.updateData(filtered)
    }

    private fun likeRecipe(recipeId: String) {
        val docRef = firestore.collection("recipes").document(recipeId)
        firestore.runTransaction { t ->
            val snapshot = t.get(docRef)
            val likes = (snapshot.getLong("likes") ?: 0L) + 1
            t.update(docRef, "likes", likes)
        }.addOnSuccessListener { loadRecipes() }
    }

    private fun dislikeRecipe(recipeId: String) {
        val docRef = firestore.collection("recipes").document(recipeId)
        firestore.runTransaction { t ->
            val snapshot = t.get(docRef)
            val dislikes = (snapshot.getLong("dislikes") ?: 0L) + 1
            t.update(docRef, "dislikes", dislikes)
        }.addOnSuccessListener { loadRecipes() }
    }

    private fun openRecipeDetails(recipe: Recipe) {
        val intent = Intent(requireContext(), RecipeDetailsActivity::class.java)
        intent.putExtra("recipe_id", recipe.id)
        intent.putExtra("title", recipe.title)
        intent.putExtra("description", recipe.description)
        intent.putStringArrayListExtra("ingredients", ArrayList(recipe.ingredients))
        intent.putExtra("likes", recipe.likes)
        intent.putExtra("dislikes", recipe.dislikes)
        startActivity(intent)
    }
}
