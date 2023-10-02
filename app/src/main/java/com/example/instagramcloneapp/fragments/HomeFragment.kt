package com.example.instagramcloneapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramcloneapp.adapters.PostAdapter
import com.example.instagramcloneapp.databinding.FragmentHomeBinding
import com.example.instagramcloneapp.module.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var postAdapter: PostAdapter
    private var postList: MutableList<Post> = mutableListOf()
    private var followingList: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        postAdapter = PostAdapter(requireContext(), postList)

        val recyclerView = binding.recyclerViewHome.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            adapter = postAdapter
        }

        getFollowing()

        return binding.root
    }

    private fun getFollowing() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow")
            .child(firebaseUser.uid)
            .child("Following")

        followersRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        followingList.clear()
                        snapshot.children.forEach { userSnapshot ->
                            userSnapshot.key?.let { followingList.add(it) }
                        }
                    }

                    getPosts()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getPosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return

                    postList.clear()
                    snapshot.children.forEach { postSnapshot ->
                        val post = postSnapshot.getValue(Post::class.java) ?: return@forEach
                        if (post.publisher in followingList || post.publisher == firebaseUser.uid)
                            postList.add(post)
                    }
                    postAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}