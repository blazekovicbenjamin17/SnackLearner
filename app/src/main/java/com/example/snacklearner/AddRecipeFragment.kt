package com.example.snacklearner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class AddRecipeFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val titleEditText = view.findViewById<EditText>(R.id.titleEditText)
        val descriptionEditText = view.findViewById<EditText>(R.id.descriptionEditText)
        val ingredientsEditText = view.findViewById<EditText>(R.id.ingredientsEditText)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val backButton = view.findViewById<Button>(R.id.backButton)


        // Unutar tvog fragmenta:
        backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SearchFragment()) // postavi početni fragment
                .commit()
        }



        // Spremanje recepta
        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val ingredients = ingredientsEditText.text.toString().trim()

            if (title.isEmpty() || description.isEmpty() || ingredients.isEmpty()) {
                Toast.makeText(requireContext(), "Popuni sva polja.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(requireContext(), "Morate biti prijavljeni.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Dohvati username iz Firestore-a
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { userDoc ->
                    val username = userDoc.getString("username") ?: currentUser.email ?: "Anonimno"

                    val recipeData = hashMapOf(
                        "title" to title,
                        "description" to description,
                        "ingredients" to ingredients,
                        "userId" to currentUser.uid,
                        "username" to username,
                        "likes" to 0,
                        "dislikes" to 0,
                        "createdAt" to Timestamp.now()
                    )

                    firestore.collection("recipes")
                        .add(recipeData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Recept spremljen!", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack() // Povratak
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Greška pri spremanju: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Greška pri dohvaćanju podataka o korisniku: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
