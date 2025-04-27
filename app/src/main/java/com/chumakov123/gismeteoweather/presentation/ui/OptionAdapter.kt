package com.chumakov123.gismeteoweather.presentation.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

class OptionAdapter(
    private val onClick: (OptionItem) -> Unit
) : RecyclerView.Adapter<OptionAdapter.VH>() {

    private val items = mutableListOf<OptionItem>()
    fun submitList(newList: List<OptionItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle    = view.findViewById<TextView>(android.R.id.text1)
        private val tvSubtitle = view.findViewById<TextView>(android.R.id.text2)
        fun bind(item: OptionItem) {
            tvTitle.text    = item.title
            tvSubtitle.text = item.subtitle
            tvSubtitle.isVisible = !item.subtitle.isNullOrEmpty()
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return VH(v)
    }
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }
}