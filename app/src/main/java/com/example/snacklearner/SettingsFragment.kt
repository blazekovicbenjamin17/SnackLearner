package com.example.snacklearner

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val userInfoPref = findPreference<Preference>("user_info")
        val editProfilePref = findPreference<Preference>("edit_profile")
        val logoutPref = findPreference<Preference>("logout")

        // Dohvat korisničkog imena
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    userInfoPref?.summary = doc.getString("username") ?: "Nepoznati korisnik"
                }
                .addOnFailureListener {
                    userInfoPref?.summary = "Greška pri učitavanju"
                }
        } else {
            userInfoPref?.summary = "Nepoznati korisnik"
        }

        // Listener za uređivanje profila
        editProfilePref?.setOnPreferenceClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, EditProfileFragment())
                .addToBackStack(null)
                .commit()
            true
        }

        // Listener za odjavu
        logoutPref?.setOnPreferenceClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Odjavljeni ste.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .commit()
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorBackground))
    }
}
