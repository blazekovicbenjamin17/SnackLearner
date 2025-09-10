package com.example.snacklearner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AdminRecipe(
    val id: String,
    val title: String,
    val description: String,
    val username: String,
    val ingredients: String
)

class AdminRecipeAdapter(
    recipes: List<AdminRecipe>,
    private val onDeleteClicked: (String) -> Unit
) : RecyclerView.Adapter<AdminRecipeAdapter.AdminRecipeViewHolder>() {

    private var fullList: List<AdminRecipe> = recipes
    private var filteredList: List<AdminRecipe> = recipes

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminRecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_admin, parent, false)
        return AdminRecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminRecipeViewHolder, position: Int) {
        val recipe = filteredList[position]
        holder.titleTextView.text = recipe.title
        holder.descriptionTextView.text = recipe.description
        holder.usernameTextView.text = "by ${recipe.username}"
        holder.deleteButton.setOnClickListener { onDeleteClicked(recipe.id) }
    }

    override fun getItemCount() = filteredList.size

    fun updateData(newList: List<AdminRecipe>) {
        fullList = newList
        filteredList = newList
        notifyDataSetChanged()
    }

    fun filterData(query: String) {
        filteredList = if (query.isEmpty()) fullList else fullList.filter {
            it.title.contains(query, true) ||
                    it.description.contains(query, true) ||
                    it.username.contains(query, true) ||
                    it.ingredients.contains(query, true)
        }
        notifyDataSetChanged()
    }

    class AdminRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.recipeTitleTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.recipeDescriptionTextView)
        val usernameTextView: TextView = view.findViewById(R.id.recipeUsernameTextView)
        val deleteButton: Button = view.findViewById(R.id.deleteRecipeButton)
    }
}
