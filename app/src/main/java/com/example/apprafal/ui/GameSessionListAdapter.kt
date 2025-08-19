package com.example.apprafal.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.apprafal.R
import com.example.apprafal.data.GameSession


class GameSessionListAdapter : androidx.recyclerview.widget.ListAdapter<GameSession, GameSessionListAdapter.SessionViewHolder>(
    GameSessionDiffCallback()
) {

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(session: GameSession) {
            val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
            val dateString = dateFormat.format(java.util.Date(session.date))

            val textView = itemView.findViewById<TextView>(R.id.pickText)
            textView.text = buildString {
                append("📅 Sesja z $dateString")

                if (session.currentPickerId != null) {
                    append("\n👤 Aktualny gracz ID: ${session.currentPickerId}")
                }

                if (session.gameName != null) {
                    append("\n🎮 Gra: ${session.gameName}")
                } else {
                    append("\n🎮 Brak gry")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pick_item, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GameSessionDiffCallback : DiffUtil.ItemCallback<GameSession>() {
        override fun areItemsTheSame(oldItem: GameSession, newItem: GameSession) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: GameSession, newItem: GameSession) =
            oldItem == newItem
    }
}