package com.example.gamecoded.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gamecoded.R
import com.example.gamecoded.models.Lesson

class LessonAdapter(private val lessons: List<Lesson>) :
    RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

    class LessonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.lesson_title)
        val description: TextView = itemView.findViewById(R.id.lesson_description)
        val progress: TextView = itemView.findViewById(R.id.lesson_progress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]
        Log.d("LessonAdapter", "Binding: ${lesson.title}")
        holder.title.text = lesson.title
        holder.description.text = lesson.description
        holder.progress.text = "${lesson.progress}%"
    }

    override fun getItemCount(): Int = lessons.size
}
