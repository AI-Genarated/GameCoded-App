//package com.example.gamecoded
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//
//class ResultsAdapter(private var items: List<Pair<String, Double>>) :
//    RecyclerView.Adapter<ResultsAdapter.VH>() {
//
//    class VH(view: View) : RecyclerView.ViewHolder(view) {
//        val content: TextView = view.findViewById(R.id.textContent)
//        val score: TextView = view.findViewById(R.id.textScore)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
//        val v = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_result, parent, false)
//        return VH(v)
//    }
//
//    override fun onBindViewHolder(holder: VH, position: Int) {
//        val (text, sc) = items[position]
//        holder.content.text = text
//        holder.score.text = String.format("score: %.4f", sc)
//    }
//
//    override fun getItemCount(): Int = items.size
//
//    fun update(newItems: List<Pair<String, Double>>) {
//        items = newItems
//        notifyDataSetChanged()
//    }
//}
