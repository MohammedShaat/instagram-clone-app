package com.example.instagramcloneapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.CommentsActivity
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

    private val firebaseUser = FirebaseAuth.getInstance().currentUser!!

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
                isLiked(post.id, postImageLikeBtn)
                isSaved(post.id)
                getNumberOfLikes(post.id)
                getNumberOfComments(post.id)

                postImageLikeBtn.setOnClickListener {
                    if (postImageLikeBtn.tag == "Like") {
                        FirebaseDatabase.getInstance().reference
                            .child("Likes")
                            .child(post.id)
                            .child(firebaseUser.uid)
                            .setValue(true)
                    } else {
                        FirebaseDatabase.getInstance().reference
                            .child("Likes")
                            .child(post.id)
                            .child(firebaseUser.uid)
                            .removeValue()
                    }
                }

                postImageCommentBtn.setOnClickListener {
                    val intent = Intent(mContext, CommentsActivity::class.java).apply {
                        putExtra("postId", post.id)
                        putExtra("uid", firebaseUser.uid)
                    }
                    mContext.startActivity(intent)
                }

                comments.setOnClickListener {
                    val intent = Intent(mContext, CommentsActivity::class.java).apply {
                        putExtra("postId", post.id)
                        putExtra("uid", firebaseUser.uid)
                    }
                    mContext.startActivity(intent)
                }

                postSaveCommentBtn.setOnClickListener {
                    savePost(post.id)
                }
            }
        }

        private fun getNumberOfLikes(postId: String) {
            val postIdRef = FirebaseDatabase.getInstance().reference
                .child("Likes")
                .child(postId)
            postIdRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.likes.text = "${snapshot.childrenCount} Likes"
                    } else {
                        binding.likes.text = "Be the first to like"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun getNumberOfComments(postId: String) {
            val postRef = FirebaseDatabase.getInstance().reference
                .child("Comments")
                .child(postId)

            postRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.comments.text = "Click to view ${snapshot.childrenCount} comments"
                    } else {
                        binding.comments.text = "Click to be the first to comment"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun isLiked(postId: String, likeBtn: ImageView) {
            val liked = FirebaseDatabase.getInstance().reference
                .child("Likes")
                .child(postId)
                .child(firebaseUser.uid)

            liked.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        likeBtn.setImageResource(R.drawable.heart_clicked)
                        likeBtn.tag = "Liked"
                    } else {
                        likeBtn.setImageResource(R.drawable.heart_not_clicked)
                        likeBtn.tag = "Like"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
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

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun savePost(id: String) {
            val postRef = FirebaseDatabase.getInstance().reference
                .child("Saved_Posts")
                .child(firebaseUser.uid)
                .child(id)

            if (binding.postSaveCommentBtn.tag == "save") {
                postRef.setValue(true)
            } else {
                postRef.removeValue()
            }
        }

        private fun isSaved(postId: String) {
            val postRef = FirebaseDatabase.getInstance().reference
                .child("Saved_Posts")
                .child(firebaseUser.uid)
                .child(postId)

            postRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.postSaveCommentBtn.apply {
                            setImageResource(R.drawable.save_large_icon)
                            tag = "saved"
                        }
                    } else {
                        binding.postSaveCommentBtn.apply {
                            setImageResource(R.drawable.save_unfilled_large_icon)
                            tag = "save"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}
