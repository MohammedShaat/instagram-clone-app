package com.example.instagramcloneapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.databinding.NotificationItemBinding
import com.example.instagramcloneapp.fragments.PostDetailsFragment
import com.example.instagramcloneapp.fragments.ProfileFragment
import com.example.instagramcloneapp.module.Notification
import com.example.instagramcloneapp.module.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class NotificationAdapter(
    private val mContext: Context,
    private val mNotification: List<Notification>,
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private var firebaseNotification = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = mNotification[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int {
        return mNotification.size
    }

    inner class ViewHolder(private val binding: NotificationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.apply {
                getUserInfo(notification.userId)
                commentNotification.text = notification.text
                if (notification.isPost)
                    getPostImage(notification.postId!!)

                root.setOnClickListener {
                    if (notification.isPost) {
                        val pref =
                            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                        pref.putString("postId", notification.postId)
                        pref.apply()
                        (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, PostDetailsFragment()).commit()
                    } else {
                        val pref =
                            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                        pref.putString("profileId", notification.userId)
                        pref.apply()
                        (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, ProfileFragment()).commit()
                    }
                }

                notificationProfileImage.setOnClickListener {
                    val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                    pref.putString("profileId", notification.userId)
                    pref.apply()
                    (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment()).commit()
                }
            }
        }

        private fun getUserInfo(userId: String) {
            val userRef = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userId)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)!!
                    binding.apply {
                        Picasso.get().load(user.image).placeholder(R.drawable.profile)
                            .into(notificationProfileImage)
                        userNameNotification.text = user.fullname
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun getPostImage(postId: String) {
            val imageRef = FirebaseDatabase.getInstance().reference
                .child("Posts")
                .child(postId)
                .child("image")

            imageRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val imageUri = snapshot.getValue(String::class.java)
                    Picasso.get().load(imageUri).placeholder(R.drawable.profile)
                        .into(binding.notificationPostImage)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

}