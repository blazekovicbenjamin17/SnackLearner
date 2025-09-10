package com.example.snacklearner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileFragment : Fragment() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var currentPasswordEditText: TextInputEditText
    private lateinit var newPasswordEditText: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailEditText = view.findViewById(R.id.emailEditText)
        usernameEditText = view.findViewById(R.id.usernameEditText)
        firstNameEditText = view.findViewById(R.id.firstNameEditText)
        lastNameEditText = view.findViewById(R.id.lastNameEditText)
        currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText)
        saveButton = view.findViewById(R.id.saveButton)
        backButton = view.findViewById(R.id.backButton)

        // Postavi email korisnika
        auth.currentUser?.let { user ->
            emailEditText.setText(user.email)
        }

        // Učitaj ostale podatke iz Firestore
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    usernameEditText.setText(doc.getString("username") ?: "")
                    firstNameEditText.setText(doc.getString("firstName") ?: "")
                    lastNameEditText.setText(doc.getString("lastName") ?: "")
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Greška pri učitavanju podataka", Toast.LENGTH_SHORT).show()
                }
        }

        saveButton.setOnClickListener {
            saveProfileChanges()
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun saveProfileChanges() {
        val uid = auth.currentUser?.uid ?: return
        val username = usernameEditText.text.toString().trim()
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val currentPassword = currentPasswordEditText.text.toString()
        val newPassword = newPasswordEditText.text.toString()

        // Ažuriraj Firestore podatke
        val updates = hashMapOf(
            "username" to username,
            "firstName" to firstName,
            "lastName" to lastName
        )
        firestore.collection("users").document(uid)
            .update(updates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profil spremljen", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri spremanju", Toast.LENGTH_SHORT).show()
            }

        // Ako korisnik želi promijeniti lozinku
        if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
            val user = auth.currentUser
            if (user != null && user.email != null) {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        user.updatePassword(newPassword).addOnCompleteListener { updateResult ->
                            if (updateResult.isSuccessful) {
                                Toast.makeText(requireContext(), "Lozinka promijenjena", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Greška pri promjeni lozinke", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Trenutna lozinka nije točna", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}