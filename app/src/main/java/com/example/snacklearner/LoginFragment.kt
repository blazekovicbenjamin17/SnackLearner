// LoginFragment.kt
package com.example.snacklearner

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val activity = requireActivity() as MainActivity
        activity.getToolbar().visibility = View.GONE
        activity.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        val usernameEditText = view.findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val registerButton = view.findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            val email = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Upiši email i lozinku.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideKeyboard(view)
                        val uid = auth.currentUser!!.uid
                        firestore.collection("users").document(uid).get()
                            .addOnSuccessListener { doc ->
                                val role = doc.getString("role") ?: "user"
                                val isAdmin = role == "admin"

                                activity.getToolbar().visibility = View.VISIBLE
                                activity.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

                                if (isAdmin) {
                                    Toast.makeText(requireContext(), "Prijavljen admin.", Toast.LENGTH_SHORT).show()
                                    val bundle = Bundle()
                                    bundle.putBoolean("isAdmin", true)
                                    val adminFragment = AdminFragment()
                                    adminFragment.arguments = bundle
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.fragmentContainer, adminFragment)
                                        .commit()
                                } else {
                                    Toast.makeText(requireContext(), "Prijavljen korisnik.", Toast.LENGTH_SHORT).show()
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.fragmentContainer, SearchFragment())
                                        .commit()
                                }
                            }
                    } else {
                        Toast.makeText(requireContext(), "Greška: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        registerButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
