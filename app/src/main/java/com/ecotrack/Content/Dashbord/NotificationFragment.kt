package com.ecotrack.Content.Dashbord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import com.ecotrack.Content.Dashbord.Adapter.Notification
import com.ecotrack.Content.Dashbord.Adapter.NotificationAdapter
import com.ecotrack.MainActivity
import com.ecotrack.R
import com.ecotrack.content.dashboard.HomeFragment

class NotificationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationList = listOf(
        Notification(R.drawable.ringing, "Carbon Footprint Update", "Your carbon footprint has decreased by 15% this week. Great job!", "1/5/2025"),
        Notification(R.drawable.ringing, "Energy Consumption Update", "Your energy consumption has increased by 10% this month.", "30/4/2025")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        // Set up RecyclerView for notifications
        recyclerView = view.findViewById(R.id.notificationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationAdapter = NotificationAdapter(notificationList)
        recyclerView.adapter = notificationAdapter

        // Set up the back button functionality
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            // Navigate back to the previous fragment
            (activity as? MainActivity)?.showFragment(HomeFragment())
        }

        return view
    }
}
