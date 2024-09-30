package com.example.booknook.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.R

class GenresAdapter(private val genresList: List<String>) : RecyclerView.Adapter<GenresAdapter.GenreViewHolder>() {

    private val selectedGenres: MutableSet<String> = mutableSetOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_genre, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = genresList[position]
        holder.genreCheckBox.text = genre

        // Handle checkbox state persistence
        holder.genreCheckBox.isChecked = selectedGenres.contains(genre)

        holder.genreCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedGenres.add(genre)
            } else {
                selectedGenres.remove(genre)
            }
        }
    }

    override fun getItemCount(): Int {
        return genresList.size
    }

    fun getSelectedGenres(): Set<String> {
        return selectedGenres
    }

    class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val genreCheckBox: CheckBox = itemView.findViewById(R.id.genreCheckBox)
    }
}
