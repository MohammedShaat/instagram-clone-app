package com.example.instagramcloneapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.databinding.UserItemLayoutBinding
import com.example.instagramcloneapp.fragments.ProfileFragment
import com.example.instagramcloneapp.module.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class UserAdapter(
    private val mContext: Context,
    private val mUser: List<User>,
    private val isFragment: Boolean = true
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var firebaseUser = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            UserItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUser[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    inner class ViewHolder(private val binding: UserItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                userFullNameSearch.text = user.fullname
                userNameSearch.text = user.username

                Picasso.get().load(user.image).placeholder(R.drawable.profile)
                    .into(userProfileImage)

                checkFollowingStatus(user.uid, followBtnSearch)

                itemView.setOnClickListener {
                    if (!isFragment) return@setOnClickListener

                    val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                    pref.putString("profileId", user.uid)
                    pref.apply()
                    (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment()).commit()
                }

                followBtnSearch.setOnClickListener {
                    followOrUnFollow(user)
                }
            }
        }

        private fun followOrUnFollow(user: User) {
            if (binding.followBtnSearch.text.toString() == "Follow") {
                FirebaseDatabase.getInstance().reference
                    .child("Follow")
                    .child(firebaseUser.uid)
                    .child("Following")
                    .child(user.uid)
                    .setValue(true)

                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow")
                            .child(user.uid)
                            .child("Followers")
                            .child(firebaseUser.uid)
                            .setValue(true)

                            .addOnSuccessListener {
                                val notificationRef = FirebaseDatabase.getInstance().reference
                                    .child("Notifications")
                                    .child(user.uid)
                                    .push()
                                val notificationMap = hashMapOf<String, Any?>()
                                notificationMap["id"] = notificationRef.key!!
                                notificationMap["text"] = "followed you"
                                notificationMap["post_id"] = null
                                notificationMap["user_id"] = firebaseUser.uid
                                notificationMap["is_post"] = false

                                notificationRef.setValue(notificationMap)
                            }
                    }
            } else {
                FirebaseDatabase.getInstance().reference
                    .child("Follow")
                    .child(firebaseUser.uid)
                    .child("Following")
                    .child(user.uid)
                    .removeValue()
                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().reference
                            .child("Follow")
                            .child(user.uid)
                            .child("Followers")
                            .child(firebaseUser.uid)
                            .removeValue()
                    }
            }
        }
    }

    private fun checkFollowingStatus(uid: String, followBtnSearch: Button) {
        if (firebaseUser.uid == uid) {
            followBtnSearch.isVisible = false
            return
        }

        val followingRef =
            FirebaseDatabase.getInstance().reference
                .child("Follow")
                .child(firebaseUser.uid)
                .child("Following")

        followingRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(uid).exists()) {
                    followBtnSearch.text = "Following"
                } else {
                    followBtnSearch.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}