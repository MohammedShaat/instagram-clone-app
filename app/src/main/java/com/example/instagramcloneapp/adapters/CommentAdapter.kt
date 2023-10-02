package com.example.instagramcloneapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.databinding.CommentItemBinding
import com.example.instagramcloneapp.module.Comment
import com.example.instagramcloneapp.module.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class CommentAdapter(
    private val mContext: Context,
    private val mComments: List<Comment>,
    private val isFragment: Boolean = false
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        holder.bind(mComments[position])
    }

    override fun getItemCount(): Int {
        return mComments.count()
    }

    inner class ViewHolder(private val binding: CommentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.apply {
                getUserInfo(comment.commenterId)
                commentTextView.text = comment.comment
            }
        }

        private fun getUserInfo(commenterId: String) {
            val userRef = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(commenterId)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java) ?: return
                    binding.apply {
                        Picasso.get().load(user.image).into(userImage)
                        userName.text = user.fullname
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}
