package com.example.instagramcloneapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramcloneapp.adapters.CommentAdapter
import com.example.instagramcloneapp.databinding.ActivityCommentsBinding
import com.example.instagramcloneapp.module.Comment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class CommentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentsBinding

    private lateinit var postId: String
    private lateinit var uid: String
    private lateinit var publisherId: String

    private val commentList: MutableList<Comment> = mutableListOf()
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = requireNotNull(intent.getStringExtra("postId"))
        uid = requireNotNull(intent.getStringExtra("uid"))
        publisherId = requireNotNull(intent.getStringExtra("publisherId"))
        commentAdapter = CommentAdapter(applicationContext, commentList)

        binding.recyclerViewComments.apply {
            layoutManager = LinearLayoutManager(applicationContext).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            adapter = commentAdapter
        }

        getUserImage()
        getPostImage()
        getComments()

        binding.postCommentBtn.setOnClickListener {
            publishComment()
        }
    }

    private fun getUserImage() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageUri = snapshot.child("image").getValue(String::class.java) ?: return
                Picasso.get().load(imageUri).into(binding.profileImageComments)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getPostImage() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .child(postId)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageUri = snapshot.child("image").getValue(String::class.java) ?: return
                Picasso.get().load(imageUri).into(binding.postImageComments)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getComments() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                commentList.clear()
                snapshot.children.forEach { commentSnapshot ->
                    val comment = commentSnapshot.getValue(Comment::class.java) ?: return
                    commentList.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun publishComment() {
        val commentTextView = binding.addCommentEditText
        if (commentTextView.text.isBlank()) {
            Toast.makeText(applicationContext, "Please type anything", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
            setTitle("Publishing comment")
            setMessage("We are publishing your comment. Please wait.")
            show()
        }

        val commentRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId)
            .push()

        val commentInfo = hashMapOf<String, String>()
        commentInfo["id"] = commentRef.key!!
        commentInfo["commenterId"] = uid
        commentInfo["comment"] = commentTextView.text.toString()

        commentRef.setValue(commentInfo)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    commentTextView.setText("")
                    Toast.makeText(
                        applicationContext,
                        "Comment has been published successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    val notificationRef = FirebaseDatabase.getInstance().reference
                        .child("Notifications")
                        .child(publisherId)
                        .push()
                    val notificationMap = hashMapOf<String, Any?>()
                    notificationMap["id"] = notificationRef.key!!
                    notificationMap["text"] = "commented on your post"
                    notificationMap["post_id"] = postId
                    notificationMap["user_id"] = uid
                    notificationMap["is_post"] = true
                    notificationRef.setValue(notificationMap)
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(
                        applicationContext,
                        "Comment has not been published",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}
