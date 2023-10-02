package com.example.instagramcloneapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.adapters.PostAdapter
import com.example.instagramcloneapp.databinding.FragmentPostDetailsBinding
import com.example.instagramcloneapp.module.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class PostDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPostDetailsBinding

    private lateinit var postId: String
    private val postList = mutableListOf<Post>()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPostDetailsBinding.inflate(inflater)

        val pref = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        postId = requireNotNull(pref.getString("postId", ""))

        postAdapter = PostAdapter(requireContext(), postList)
        binding.recyclerViewPostDetails.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }

        getPostInfo()

        return binding.root
    }

    private fun getPostInfo() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .child(postId)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                postList.clear()
                val post = snapshot.getValue<Post>()!!
                postList.add(post)
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}