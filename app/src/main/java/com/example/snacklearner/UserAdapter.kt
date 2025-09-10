package com.example.snacklearner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog

class UserAdapter(
    private var users: List<Triple<String, String, String>>, // email, role, uid
    private val onDeleteClicked: (String) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var usersFullList: List<Triple<String, String, String>> = users
    private var isAdminMode: Boolean = false

    fun updateData(newUsers: List<Triple<String, String, String>>) {
        users = newUsers
        usersFullList = newUsers
        notifyDataSetChanged()
    }

    fun setAdminMode(isAdmin: Boolean) {
        isAdminMode = isAdmin
        notifyDataSetChanged()
    }

    fun filterData(query: String) {
        users = if (query.isEmpty()) usersFullList else usersFullList.filter { (email, role, _) ->
            email.contains(query, ignoreCase = true) || role.contains(query, ignoreCase = true)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val (email, role, uid) = users[position]
        holder.emailTextView.text = email
        holder.roleTextView.text = role

        if (isAdminMode) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                val context = holder.itemView.context
                AlertDialog.Builder(context)
                    .setTitle("Potvrda")
                    .setMessage("Jeste li sigurni da želite obrisati ovog korisnika?")
                    .setPositiveButton("Da") { _, _ -> onDeleteClicked(uid) }
                    .setNegativeButton("Ne", null)
                    .show()
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }
    }

    override fun getItemCount() = users.size

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emailTextView: TextView = view.findViewById(R.id.userEmailTextView)
        val roleTextView: TextView = view.findViewById(R.id.userRoleTextView)
        val deleteButton: Button = view.findViewById(R.id.deleteUserButton)
    }
}
