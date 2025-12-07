package com.example.qash_finalproject.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qash_finalproject.R

// Data Model Sederhana
data class InboxModel(
    val title: String,
    val message: String,
    val date: String,
    val isRead: Boolean = false
)

class InboxAdapter(private val listInbox: List<InboxModel>) :
    RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    inner class InboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val dotUnread: View = itemView.findViewById(R.id.dot_unread)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inbox, parent, false)
        return InboxViewHolder(view)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        val item = listInbox[position]
        holder.tvTitle.text = item.title
        holder.tvMessage.text = item.message
        holder.tvDate.text = item.date

        // Sembunyikan titik merah jika sudah dibaca
        if (item.isRead) {
            holder.dotUnread.visibility = View.INVISIBLE
        } else {
            holder.dotUnread.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = listInbox.size
}