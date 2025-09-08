package com.example.snacklearner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val username: String,
    val ingredients: String,
    val likes: Int,
    val dislikes: Int
)

class RecipeAdapter(
    recipes: List<Recipe>,
    private val onLikeClicked: (String) -> Unit,
    private val onDislikeClicked: (String) -> Unit,
    private val onRecipeClicked: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private var fullList: List<Recipe> = recipes
    private var recipesFiltered: List<Recipe> = recipes

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.recipeTitleTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.recipeDescriptionTextView)
        val usernameTextView: TextView = view.findViewById(R.id.recipeUsernameTextView)
        val likesTextView: TextView = view.findViewById(R.id.recipeLikesTextView)
        val dislikesTextView: TextView = view.findViewById(R.id.recipeDislikesTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipesFiltered[position]

        holder.titleTextView.text = recipe.title
        holder.descriptionTextView.text = recipe.description
        holder.usernameTextView.text = "by ${recipe.username}"
        holder.likesTextView.text = "👍 ${recipe.likes}"
        holder.dislikesTextView.text = "👎 ${recipe.dislikes}"

        holder.likesTextView.setOnClickListener { onLikeClicked(recipe.id) }
        holder.dislikesTextView.setOnClickListener { onDislikeClicked(recipe.id) }
        holder.itemView.setOnClickListener { onRecipeClicked(recipe) }
    }

    override fun getItemCount(): Int = recipesFiltered.size

    fun updateData(newList: List<Recipe>) {
        fullList = newList
        recipesFiltered = newList
        notifyDataSetChanged()
    }

    fun filterData(query: String) {
        recipesFiltered = if (query.isEmpty()) {
            fullList
        } else {
            fullList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.username.contains(query, ignoreCase = true) ||
                        it.ingredients.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}
