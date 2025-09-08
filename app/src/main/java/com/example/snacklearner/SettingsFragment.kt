package com.example.snacklearner

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
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

        // Dodaj listener za uređivanje profila
        val editProfilePref = findPreference<Preference>("edit_profile")
        editProfilePref?.setOnPreferenceClickListener {
            // Prebaci na EditProfileFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, EditProfileFragment()) // Napravi ovaj fragment
                .addToBackStack(null)
                .commit()
            true
        }

        val logoutPref = findPreference<Preference>("logout")
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
        view.setBackgroundColor(Color.parseColor("#F5F5DC"))
        super.onViewCreated(view, savedInstanceState)
    }
}
