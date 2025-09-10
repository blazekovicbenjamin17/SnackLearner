package com.example.snacklearner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val onLikeClicked: (String) -> Unit,
    private val onDislikeClicked: (String) -> Unit,
    private val onRecipeClicked: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.recipeTitleTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.recipeDescriptionTextView)
        val usernameTextView: TextView = view.findViewById(R.id.recipeUsernameTextView)
        val likeButton: MaterialButton = view.findViewById(R.id.likeImageView)
        val dislikeButton: MaterialButton = view.findViewById(R.id.dislikeImageView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.titleTextView.text = recipe.title
        holder.descriptionTextView.text = recipe.description
        holder.usernameTextView.text = "by ${recipe.username}"
        holder.likeButton.text = recipe.likes.toString()
        holder.dislikeButton.text = recipe.dislikes.toString()


        holder.likeButton.setOnClickListener { onLikeClicked(recipe.id) }
        holder.dislikeButton.setOnClickListener { onDislikeClicked(recipe.id) }
        holder.itemView.setOnClickListener { onRecipeClicked(recipe) }
    }

    override fun getItemCount(): Int = recipes.size

    fun updateData(newList: List<Recipe>) {
        recipes = newList
        notifyDataSetChanged()
    }
}
