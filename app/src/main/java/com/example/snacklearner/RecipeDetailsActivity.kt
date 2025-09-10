package com.example.snacklearner

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipeDetailsActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var likeButton: MaterialButton
    private lateinit var dislikeButton: MaterialButton
    private lateinit var saveButton: Button
    private lateinit var removeButton: Button
    private lateinit var backButton: Button

    private var currentLikes = 0
    private var currentDislikes = 0
    private var recipeId = ""
    private var ingredientsList: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recipeId = intent.getStringExtra("recipe_id") ?: ""
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        ingredientsList = intent.getStringArrayListExtra("ingredients")?.mapNotNull { it } ?: emptyList()
        currentLikes = intent.getIntExtra("likes", 0)
        currentDislikes = intent.getIntExtra("dislikes", 0)

        findViewById<TextView>(R.id.titleTextView).text = title
        findViewById<TextView>(R.id.descriptionTextView).text = description
        findViewById<TextView>(R.id.ingredientsTextView).text = ingredientsList.joinToString("\n")

        likeButton = findViewById(R.id.likeButton)
        dislikeButton = findViewById(R.id.dislikeButton)
        saveButton = findViewById(R.id.saveFavoriteButton)
        removeButton = findViewById(R.id.removeFavoriteButton)
        backButton = findViewById(R.id.backButton)

        updateLikesDislikesUI()

        likeButton.setOnClickListener { updateLike(true) }
        dislikeButton.setOnClickListener { updateLike(false) }
        backButton.setOnClickListener { finish() }

        setupFavoriteManagement()
    }

    private fun setupFavoriteManagement() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Prijavite se da spremite favorite.", Toast.LENGTH_SHORT).show()
            saveButton.isEnabled = false
            removeButton.isEnabled = false
        } else {
            val savedRef = firestore.collection("favorites")
                .document(userId)
                .collection("savedRecipes")
                .document(recipeId)

            savedRef.get().addOnSuccessListener { doc ->
                val isSaved = doc.exists()
                saveButton.visibility = if (isSaved) View.GONE else View.VISIBLE
                removeButton.visibility = if (isSaved) View.VISIBLE else View.GONE
            }

            saveButton.setOnClickListener {
                savedRef.set(mapOf("savedAt" to System.currentTimeMillis()))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Spremljeno u favorite.", Toast.LENGTH_SHORT).show()
                        saveButton.visibility = View.GONE
                        removeButton.visibility = View.VISIBLE
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Greška pri spremanju.", Toast.LENGTH_SHORT).show()
                    }
            }

            removeButton.setOnClickListener {
                savedRef.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Uklonjeno iz favorita.", Toast.LENGTH_SHORT).show()
                        saveButton.visibility = View.VISIBLE
                        removeButton.visibility = View.GONE
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Greška pri uklanjanju.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun updateLike(isLike: Boolean) {
        val docRef = firestore.collection("recipes").document(recipeId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            if (isLike) {
                val newLikes = (snapshot.getLong("likes") ?: 0L) + 1
                transaction.update(docRef, "likes", newLikes)
                currentLikes += 1
            } else {
                val newDislikes = (snapshot.getLong("dislikes") ?: 0L) + 1
                transaction.update(docRef, "dislikes", newDislikes)
                currentDislikes += 1
            }
        }.addOnSuccessListener { updateLikesDislikesUI() }
            .addOnFailureListener { Toast.makeText(this, "Greška pri ažuriranju.", Toast.LENGTH_SHORT).show() }
    }

    private fun updateLikesDislikesUI() {
        likeButton.text = currentLikes.toString()
        dislikeButton.text = currentDislikes.toString()
    }
}
