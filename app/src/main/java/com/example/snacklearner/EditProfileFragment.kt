package com.example.snacklearner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val firstNameEditText = view.findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = view.findViewById<EditText>(R.id.lastNameEditText)
        val usernameEditText = view.findViewById<EditText>(R.id.usernameEditText)
        val newPasswordEditText = view.findViewById<EditText>(R.id.newPasswordEditText)
        val currentPasswordEditText = view.findViewById<EditText>(R.id.currentPasswordEditText)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val backButton = view.findViewById<Button>(R.id.backButton) // Novi back gumb

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Nisi prijavljen.", Toast.LENGTH_SHORT).show()
            return
        }

        // Učitaj podatke
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                firstNameEditText.setText(doc.getString("firstName") ?: "")
                lastNameEditText.setText(doc.getString("lastName") ?: "")
                usernameEditText.setText(doc.getString("username") ?: "")
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri učitavanju podataka.", Toast.LENGTH_SHORT).show()
            }

        saveButton.setOnClickListener {
            val newFirstName = firstNameEditText.text.toString().trim()
            val newLastName = lastNameEditText.text.toString().trim()
            val newUsername = usernameEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val currentPassword = currentPasswordEditText.text.toString().trim()

            val updates = mutableMapOf<String, Any>()
            if (newFirstName.isNotEmpty()) updates["firstName"] = newFirstName
            if (newLastName.isNotEmpty()) updates["lastName"] = newLastName
            if (newUsername.isNotEmpty()) updates["username"] = newUsername

            firestore.collection("users").document(userId).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Podaci ažurirani.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Greška pri ažuriranju podataka.", Toast.LENGTH_SHORT).show()
                }

            if (newPassword.isNotEmpty()) {
                if (currentPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Unesi trenutnu lozinku.", Toast.LENGTH_SHORT).show()
                } else {
                    val user = auth.currentUser!!
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            user.updatePassword(newPassword)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Lozinka ažurirana.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Greška pri ažuriranju lozinke.", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Neispravna trenutna lozinka.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        // Logika za back gumb
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
