package com.example.snacklearner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var isAdmin = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.usersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = UserAdapter(
            emptyList(),
            onDeleteClicked = { uid -> deleteUser(uid) }
        )
        recyclerView.adapter = adapter

        // Dohvati je li admin iz argumenata
        isAdmin = arguments?.getBoolean("isAdmin") == true
        adapter.setAdminMode(isAdmin)

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .commit()
        }

        val enterAppButton = view.findViewById<Button>(R.id.enterAppButton)
        enterAppButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SearchFragment())
                .commit()
        }

        if (isAdmin) {
            loadUsers()
        } else {
            Toast.makeText(
                requireContext(),
                "Niste admin – samo pregled aplikacije.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadUsers() {
        firestore.collection("users").get()
            .addOnSuccessListener { result ->
                val users = result.documents
                    .map { doc ->
                        val email = doc.getString("email") ?: ""
                        val role = doc.getString("role") ?: "user"
                        Triple(email, role, doc.id)
                    }
                    .filter { (_, role, _) -> role != "admin" } // sakrij druge admine
                adapter.updateData(users)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Greška: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun deleteUser(uid: String) {
        if (!isAdmin) {
            Toast.makeText(requireContext(), "Nemate ovlasti.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").document(uid).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Korisnik obrisan.", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
