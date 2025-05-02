package com.ecotrack.content.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import com.google.android.material.tabs.TabLayout
import com.ecotrack.R
import androidx.fragment.app.FragmentTransaction
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import com.ecotrack.Content.Dashbord.AnalyticsFragment
import com.ecotrack.Content.Dashbord.DashboardFragment
import com.ecotrack.Content.Dashbord.ProfileFragment
import com.ecotrack.Content.Dashbord.NotificationFragment
import com.ecotrack.MainActivity
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils.attachBadgeDrawable
import com.google.android.material.badge.ExperimentalBadgeUtils

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @OptIn(ExperimentalBadgeUtils::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout: TabLayout = view.findViewById(R.id.tabLayout)

        // Set up toolbar and notification badge
        val toolbar: Toolbar = view.findViewById(R.id.topAppBar)
        toolbar.inflateMenu(R.menu.top_app_bar)
        // Get the notification MenuItem
        val notificationMenuItem = toolbar.menu.findItem(R.id.notification)

        // Set up click listener for the notification menu item
        notificationMenuItem.setOnMenuItemClickListener {
            (activity as? MainActivity)?.showFragment(NotificationFragment())
            true
        }

        // Create the BadgeDrawable
        val badgeDrawable = BadgeDrawable.create(requireContext()).apply {
            number = 2  // Set the badge number (e.g., 2 new notifications)
            isVisible = true  // Make the badge visible
            badgeGravity = BadgeDrawable.TOP_END  // Position the badge at the top-right
            backgroundColor = ContextCompat.getColor(requireContext(), R.color.red)  // Set badge background color
        }

        // Attach the badge to the menu item
        val actionView = notificationMenuItem.actionView
        if (actionView != null) {
            // If the menu item has an action view, attach the badge to it
            attachBadgeDrawable(badgeDrawable, actionView, null)
        } else {
            // Attach the badge to the toolbar itself at the menu item position
            attachBadgeDrawable(badgeDrawable, toolbar, notificationMenuItem.itemId)
        }

        // Set default fragment to DashboardFragment
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())  // Set the default fragment
        }

        // Initialize the first tab to have a white icon by default
        val firstTab = tabLayout.getTabAt(0)
        firstTab?.icon?.setTint(ContextCompat.getColor(requireContext(), R.color.white))

        // Set up tab selected listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Change icon tint and text color for selected tab
                tab?.icon?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
                tab?.view?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))

                when (tab?.position) {
                    0 -> replaceFragment(DashboardFragment()) // Dashboard Tab
                    1 -> replaceFragment(AnalyticsFragment()) // Analytics Tab
                    2 -> replaceFragment(ProfileFragment()) // Profile Tab
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Reset icon tint and text color for unselected tab
                tab?.icon?.setTint(ContextCompat.getColor(requireContext(), R.color.dark_gray))
                tab?.view?.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle reselection of the tab if necessary
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }
}