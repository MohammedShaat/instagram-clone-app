package com.example.instagramcloneapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramcloneapp.adapters.NotificationAdapter
import com.example.instagramcloneapp.databinding.FragmentNotificationsBinding
import com.example.instagramcloneapp.module.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationList: MutableList<Notification> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNotificationsBinding.inflate(inflater)

        firebaseUser = requireNotNull(FirebaseAuth.getInstance().currentUser)

        notificationAdapter = NotificationAdapter(requireContext(), notificationList)
        binding.recyclerViewNotifications.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext()).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            adapter = notificationAdapter
        }

        getNotifications()

        return binding.root
    }

    private fun getNotifications() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(firebaseUser.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationList.clear()
                notificationList.addAll(
                    snapshot.children.map { it.getValue(Notification::class.java)!! }
                )
                notificationAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}