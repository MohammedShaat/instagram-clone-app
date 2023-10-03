package com.example.instagramcloneapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramcloneapp.adapters.UserAdapter
import com.example.instagramcloneapp.databinding.ActivityShowUsersBinding
import com.example.instagramcloneapp.module.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowUsersBinding

    private lateinit var title: String
    private lateinit var postId: String
    private lateinit var profileId: String

    private val userIdList = mutableListOf<String>()
    private val userList = mutableListOf<User>()
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShowUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = requireNotNull(intent.getStringExtra("title"))
        when (title) {
            "likes" -> postId = requireNotNull(intent.getStringExtra("postId"))
            "followers" -> profileId = requireNotNull(intent.getStringExtra("profileId"))
            "following" -> profileId = requireNotNull(intent.getStringExtra("profileId"))
        }

        setSupportActionBar(binding.likesToolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.likesToolbar.setNavigationOnClickListener {
            finish()
        }

        userAdapter = UserAdapter(applicationContext, userList, false)
        binding.recyclerViewShowUsers.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@ShowUsersActivity)
            adapter = userAdapter
        }

        when (title) {
            "likes" -> getUsersOfLikes()
            "followers" -> getFollowers()
            "following" -> getFollowing()
        }
    }

    private fun getUsersInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("users")

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                snapshot.children.forEach { userSnapshot ->
                    val user = userSnapshot.getValue(User::class.java)!!
                    if (user.uid in userIdList)
                        userList.add(user)
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getUsersOfLikes() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(postId)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userIdList.clear()
                userIdList.addAll(snapshot.children.map { it.key!! })
                getUsersInfo()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getFollowers() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Follow")
            .child(profileId)
            .child("Followers")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userIdList.clear()
                userIdList.addAll(snapshot.children.map { it.key!! })
                getUsersInfo()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getFollowing() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Follow")
            .child(profileId)
            .child("Following")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userIdList.clear()
                userIdList.addAll(snapshot.children.map { it.key!! })
                getUsersInfo()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}