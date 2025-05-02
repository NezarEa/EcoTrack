package com.ecotrack.Content.Dashbord.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecotrack.R

class NotificationAdapter(private val notifications: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val notificationIcon: ImageView = itemView.findViewById(R.id.notificationIcon)
        private val notificationTitle: TextView = itemView.findViewById(R.id.notificationTitle)
        private val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessage)
        private val notificationTime: TextView = itemView.findViewById(R.id.notificationTime)

        fun bind(notification: Notification) {
            notificationIcon.setImageResource(notification.icon)
            notificationTitle.text = notification.title
            notificationMessage.text = notification.message
            notificationTime.text = notification.timeAgo
        }
    }
}
