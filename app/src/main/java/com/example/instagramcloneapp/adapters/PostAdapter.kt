package com.example.instagramcloneapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.databinding.PostItemBinding
import com.example.instagramcloneapp.module.Post
import com.example.instagramcloneapp.module.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class PostAdapter(
    private val mContext: Context,
    private val mPosts: List<Post>,
    private val isFragment: Boolean = false
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private val firebaseUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = mPosts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return mPosts.size
    }

    inner class ViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                Picasso.get().load(post.image).into(postImageHome)
                description.text = post.description
                publisherInfo(post.publisher)
            }
        }

        private fun publisherInfo(uid: String) {
            val usersRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)

            usersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return

                    val user = snapshot.getValue(User::class.java) ?: return
                    binding.apply {
                        userName.text = user.fullname
                        Picasso.get().load(user.image).placeholder(R.drawable.profile)
                            .into(userImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }
}