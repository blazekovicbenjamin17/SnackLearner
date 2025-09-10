package com.example.snacklearner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class SavedRecipesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var savedIds = listOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_saved_recipes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        view.findViewById<Button>(R.id.backButton)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SearchFragment())
                .commit()
        }

        recyclerView = view.findViewById(R.id.savedRecipesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recipeAdapter = RecipeAdapter(
            emptyList(),
            onLikeClicked = { id -> likeRecipe(id) },
            onDislikeClicked = { id -> dislikeRecipe(id) },
            onRecipeClicked = { recipe -> openRecipeDetails(recipe) }
        )
        recyclerView.adapter = recipeAdapter

        loadSavedIdsAndRecipes()
    }

    private fun loadSavedIdsAndRecipes() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("favorites")
            .document(userId)
            .collection("savedRecipes")
            .get()
            .addOnSuccessListener { result ->
                savedIds = result.documents.map { it.id }
                if (savedIds.isEmpty()) {
                    recipeAdapter.updateData(emptyList())
                } else {
                    loadRecipesByIds(savedIds)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Greška pri dohvatu spremljenih recepata.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadRecipesByIds(ids: List<String>) {
        val batches = ids.chunked(10)
        val allRecipes = mutableListOf<Recipe>()
        var batchCount = 0

        batches.forEach { batchIds ->
            firestore.collection("recipes")
                .whereIn(FieldPath.documentId(), batchIds)
                .get()
                .addOnSuccessListener { result ->
                    val recipes = result.documents.map { doc ->
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
                    allRecipes.addAll(recipes)
                    batchCount++
                    if (batchCount == batches.size) {
                        recipeAdapter.updateData(allRecipes)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Greška pri dohvaćanju detalja recepata.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun likeRecipe(recipeId: String) {
        val docRef = firestore.collection("recipes").document(recipeId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val newLikes = (snapshot.getLong("likes") ?: 0L) + 1
            transaction.update(docRef, "likes", newLikes)
        }.addOnSuccessListener { loadSavedIdsAndRecipes() }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri lajkanju.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun dislikeRecipe(recipeId: String) {
        val docRef = firestore.collection("recipes").document(recipeId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val newDislikes = (snapshot.getLong("dislikes") ?: 0L) + 1
            transaction.update(docRef, "dislikes", newDislikes)
        }.addOnSuccessListener { loadSavedIdsAndRecipes() }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri dislajkanju.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openRecipeDetails(recipe: Recipe) {
        val intent = Intent(requireContext(), RecipeDetailsActivity::class.java).apply {
            putExtra("recipe_id", recipe.id)
            putExtra("title", recipe.title)
            putExtra("description", recipe.description)
            putStringArrayListExtra("ingredients", ArrayList(recipe.ingredients))
            putExtra("likes", recipe.likes)
            putExtra("dislikes", recipe.dislikes)
        }
        startActivity(intent)
    }
}
