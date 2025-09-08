package com.example.snacklearner

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class SearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var searchEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recipeAdapter = RecipeAdapter(
            recipes = emptyList(),
            onLikeClicked = { id -> likeRecipe(id) },
            onDislikeClicked = { id -> dislikeRecipe(id) },
            onRecipeClicked = { recipe -> openRecipeDetails(recipe) }
        )
        recyclerView.adapter = recipeAdapter

        searchEditText = view.findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                recipeAdapter.filterData(s.toString())
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {}
        })

        loadRecipes()
    }


    private fun loadRecipes() {
        firestore.collection("recipes")
            .get()
            .addOnSuccessListener { result ->
                val recipes = result.documents.map { doc ->
                    Recipe(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        username = doc.getString("username") ?: "",
                        ingredients = doc.getString("ingredients") ?: "",
                        likes = (doc.getLong("likes") ?: 0).toInt(),
                        dislikes = (doc.getLong("dislikes") ?: 0).toInt()
                    )
                }
                recipeAdapter.updateData(recipes)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Greška: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun likeRecipe(recipeId: String) {
        val docRef = firestore.collection("recipes").document(recipeId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val newLikes = (snapshot.getLong("likes") ?: 0) + 1
            transaction.update(docRef, "likes", newLikes)
        }.addOnSuccessListener { loadRecipes() }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška lajk!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun dislikeRecipe(recipeId: String) {
        val docRef = firestore.collection("recipes").document(recipeId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val newDislikes = (snapshot.getLong("dislikes") ?: 0) + 1
            transaction.update(docRef, "dislikes", newDislikes)
        }.addOnSuccessListener { loadRecipes() }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška dislajk!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openRecipeDetails(recipe: Recipe) {
        val intent = Intent(requireContext(), RecipeDetailsActivity::class.java).apply {
            putExtra("recipe_id", recipe.id)
            putExtra("title", recipe.title)
            putExtra("description", recipe.description)
            putExtra("ingredients", recipe.ingredients)
            putExtra("likes", recipe.likes)
            putExtra("dislikes", recipe.dislikes)
        }
        startActivity(intent)
    }
}
