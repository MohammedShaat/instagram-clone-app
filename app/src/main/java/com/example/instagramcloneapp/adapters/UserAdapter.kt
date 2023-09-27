package com.example.instagramcloneapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.databinding.UserItemLayoutBinding
import com.example.instagramcloneapp.fragments.ProfileFragment
import com.example.instagramcloneapp.module.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class UserAdapter(
    private val mContext: Context,
    private val mUser: List<User>,
    private val isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var firebaseUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UserItemLayoutBinding.inflate(LayoutInflater.from(mContext))
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
                    val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                    pref.putString("profileId", user.uid)
                    pref.apply()
                    (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment()).commit()
                }

                followBtnSearch.setOnClickListener {
                    if (followBtnSearch.text.toString() == "Follow") {
                        firebaseUser?.uid?.let { currentUid ->
                            FirebaseDatabase.getInstance().reference
                                .child("Follow")
                                .child(currentUid)
                                .child("Following")
                                .child(user.uid)
                                .setValue(true)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        firebaseUser?.uid?.let { currentUid ->
                                            FirebaseDatabase.getInstance().reference
                                                .child("Follow")
                                                .child(user.uid)
                                                .child("Followers")
                                                .child(currentUid)
                                                .setValue(true)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                    }
                                                }

                                        }
                                    }
                                }
                        }
                    } else {
                        firebaseUser?.uid?.let { currentUid ->
                            FirebaseDatabase.getInstance().reference
                                .child("Follow")
                                .child(currentUid)
                                .child("Following")
                                .child(user.uid)
                                .removeValue()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        firebaseUser?.uid?.let { currentUid ->
                                            FirebaseDatabase.getInstance().reference
                                                .child("Follow")
                                                .child(user.uid)
                                                .child("Followers")
                                                .child(currentUid)
                                                .removeValue()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                    }
                                                }

                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    private fun checkFollowingStatus(uid: String, followBtnSearch: Button) {
        val followingRef = firebaseUser?.uid?.let { currentUid ->
            FirebaseDatabase.getInstance().reference
                .child("Follow")
                .child(currentUid)
                .child("Following")
        }

        followingRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(uid).exists()) {
                    followBtnSearch.text = "Following"
                } else {
                    followBtnSearch.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}