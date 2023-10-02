package com.example.instagramcloneapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.example.instagramcloneapp.AccountSettingsActivity
import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.adapters.MyImageAdapter
import com.example.instagramcloneapp.databinding.FragmentProfileBinding
import com.example.instagramcloneapp.module.Post
import com.example.instagramcloneapp.module.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso

const val TAG = "ProfileFragment"

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    private val postsType = MutableLiveData("myPosts")

    private lateinit var postImageAdapter: MyImageAdapter
    private val myPostList = mutableListOf<Post>()

    private lateinit var savedPostImageAdapter: MyImageAdapter
    private val savedPostIdList = mutableListOf<String>()
    private val savedPostList = mutableListOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentProfileBinding.inflate(inflater)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        profileId = requireNotNull(pref.getString("profileId", firebaseUser.uid))

        postImageAdapter = MyImageAdapter(requireContext(), myPostList)
        savedPostImageAdapter = MyImageAdapter(requireContext(), savedPostList)

        getUserInfo()
        getNumberOfFollowers()
        getNumberOfFollowing()
        getNumberOfPosts()

        binding.recyclerViewUploadPic.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = postImageAdapter
        }

        binding.recyclerViewSavedPic.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = savedPostImageAdapter
        }

        getMyPosts()
        getSavedPostsIds()

        val editAccountSettingsBtn = binding.editAccountSettingsBtn
        if (firebaseUser.uid == profileId) {
            editAccountSettingsBtn.text = "Edit Profile"
            editAccountSettingsBtn.setOnClickListener {
                val intent = Intent(context, AccountSettingsActivity::class.java)
                startActivity(intent)
            }
        } else {
            checkFollowingStatus(profileId, editAccountSettingsBtn)
            editAccountSettingsBtn.setOnClickListener {
                followOrUnFollow(editAccountSettingsBtn)
            }
        }

        binding.imagesGridViewBtn.setOnClickListener {
            postsType.value = "myPosts"
        }

        binding.imagesSaveBtn.setOnClickListener {
            postsType.value = "savedPosts"
        }

        postsType.observe(viewLifecycleOwner) { type ->
            if (type == "myPosts") {
                binding.apply {
                    recyclerViewUploadPic.isVisible = true
                    DrawableCompat.setTint(
                        imagesGridViewBtn.drawable,
                        ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    )
                    recyclerViewSavedPic.isVisible = false
                    DrawableCompat.setTint(
                        imagesSaveBtn.drawable,
                        ContextCompat.getColor(requireContext(), R.color.black)
                    )
                }
            } else {
                binding.apply {
                    recyclerViewSavedPic.isVisible = true
                    DrawableCompat.setTint(
                        imagesSaveBtn.drawable,
                        ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    )
                    recyclerViewUploadPic.isVisible = false
                    DrawableCompat.setTint(
                        imagesGridViewBtn.drawable,
                        ContextCompat.getColor(requireContext(), R.color.black)
                    )
                }
            }
        }

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        val pref = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
        pref.remove("profileId")
        pref.apply()
    }

    private fun checkFollowingStatus(uid: String, followBtnSearch: Button) {
        val followingRef = firebaseUser.uid.let { currentUid ->
            FirebaseDatabase.getInstance().reference
                .child("Follow")
                .child(currentUid)
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {
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

    private fun followOrUnFollow(editAccountSettingsBtn: Button) {
        if (editAccountSettingsBtn.text.toString() == "Follow") {
            firebaseUser.uid.let { currentUid ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow")
                    .child(currentUid)
                    .child("Following")
                    .child(profileId)
                    .setValue(true)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            firebaseUser.uid.let { currentUid ->
                                FirebaseDatabase.getInstance().reference
                                    .child("Follow")
                                    .child(profileId)
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
            firebaseUser.uid.let { currentUid ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow")
                    .child(currentUid)
                    .child("Following")
                    .child(profileId)
                    .removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            firebaseUser.uid.let { currentUid ->
                                FirebaseDatabase.getInstance().reference
                                    .child("Follow")
                                    .child(profileId)
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

    private fun getNumberOfFollowing() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow")
            .child(profileId)
            .child("Following")

        followersRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.totalFollowing.text = snapshot.childrenCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getNumberOfFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow")
            .child(profileId)
            .child("Followers")

        followersRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.totalFollowers.text = snapshot.childrenCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getUserInfo() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(profileId)

        followersRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return
                    val user = snapshot.getValue(User::class.java) ?: return
                    binding.apply {
                        fullNameProfileFrag.text = user.fullname
                        bioProfileFrag.text = user.bio
                        profileFragmentUsername.text = user.username
                        Picasso.get().load(user.image).placeholder(R.drawable.profile)
                            .into(proImageProfileFrag)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getNumberOfPosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return

                    val count = snapshot.children.count { postSnapshot ->
                        postSnapshot.getValue(Post::class.java)?.publisher == profileId
                    }
                    binding.totalPosts.text = count.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getMyPosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                myPostList.clear()
                snapshot.children.forEach { postSnapshot ->
                    val post = postSnapshot.getValue<Post>() ?: return
                    if (post.publisher == firebaseUser.uid)
                        myPostList.add(post)
                }
                myPostList.reverse()
                postImageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getSavedPostsIds() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Saved_Posts")
            .child(firebaseUser.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                savedPostIdList.clear()
                snapshot.children.forEach { postSnapshot ->
                    val postId = postSnapshot.key!!
                    savedPostIdList.add(postId)
                }
                getSavedPosts()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getSavedPosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                savedPostList.clear()
                snapshot.children.forEach { postSnapshot ->
                    val post = postSnapshot.getValue<Post>()!!
                    if (post.id in savedPostIdList)
                        savedPostList.add(post)
                }
                savedPostList.reverse()
                savedPostImageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}