package com.example.instagramcloneapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramcloneapp.adapters.PostAdapter
import com.example.instagramcloneapp.adapters.StoryAdapter
import com.example.instagramcloneapp.databinding.FragmentHomeBinding
import com.example.instagramcloneapp.module.Post
import com.example.instagramcloneapp.module.Story
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

    private lateinit var storyAdapter: StoryAdapter
    private var storyList: MutableList<Story> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        postAdapter = PostAdapter(requireContext(), postList)
        binding.recyclerViewHome.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            adapter = postAdapter
        }

        storyAdapter = StoryAdapter(requireContext(), storyList)
        binding.recyclerViewStory.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = storyAdapter
        }

        getFollowingAndPostsAndStories()

        return binding.root
    }

    private fun getFollowingAndPostsAndStories() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow")
            .child(firebaseUser.uid)
            .child("Following")

        followersRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    followingList.clear()
                    snapshot.children.forEach { userSnapshot ->
                        userSnapshot.key?.let { followingList.add(it) }
                    }
                    getPosts()
                    getStories()
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

    private fun getStories() {
        val storiesRef = FirebaseDatabase.getInstance().reference.child("Stories")

        storiesRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    storyList.clear()
                    storyList.add(Story(userId = firebaseUser.uid))

                    snapshot.children.forEach { userSnapshot ->
                        val userID = userSnapshot.key!!
                        if (!(userID in followingList || userID == firebaseUser.uid)) return

                        val activeStorySnapshot =
                            userSnapshot.children.lastOrNull { storySnapshot ->
                                storySnapshot.getValue(Story::class.java)!!.timeEnd > System.currentTimeMillis()
                            } ?: return@forEach
                        storyList.add(activeStorySnapshot.getValue(Story::class.java)!!)
                    }

                    storyAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}