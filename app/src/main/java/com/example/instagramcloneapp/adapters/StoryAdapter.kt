package com.example.instagramcloneapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.AddStoryActivity
import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.StoryActivity
import com.example.instagramcloneapp.databinding.AddStoryItemBinding
import com.example.instagramcloneapp.databinding.StoryItemBinding
import com.example.instagramcloneapp.module.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class StoryAdapter(
    private val mContext: Context,
    private val mStory: List<Story>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val firebaseUser = FirebaseAuth.getInstance().currentUser!!
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val binding =
                AddStoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AddStoryViewHolder(binding)
        } else {
            val binding =
                StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            StoryViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddStoryViewHolder)
            holder.bind(mStory[position])
        else if (holder is StoryViewHolder)
            holder.bind(mStory[position])
    }

    override fun getItemCount(): Int {
        return mStory.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1
    }

    inner class StoryViewHolder(private val binding: StoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            binding.apply {
                getUserName(story.userId)

                Picasso.get().load(story.imageUrl).placeholder(R.drawable.profile)
                    .into(storyImage)

                Picasso.get().load(story.imageUrl).placeholder(R.drawable.profile)
                    .into(storyImageSeen)

                root.setOnClickListener {
                    val intent = Intent(mContext, StoryActivity::class.java).apply {
                        putExtra("userId", story.userId)
                    }
                    mContext.startActivity(intent)
                }

                checkSeenStatus(story.userId)
            }
        }

        private fun getUserName(userId: String) {
            val fullNameRef = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userId)
                .child("fullname")

            fullNameRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.storyUserName.text = snapshot.getValue(String::class.java)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun checkSeenStatus(publisherId: String) {
//            if (firebaseUser.uid == publisherId) {
//                binding.apply {
//                    storyImageSeen.isVisible = true
//                    storyImage.isVisible = false
//                }
//                return
//            }

            val userRef = FirebaseDatabase.getInstance().reference
                .child("Stories")
                .child(publisherId)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    snapshot.children.forEach { storySnapshot ->
                        val story = storySnapshot.getValue(Story::class.java)!!

                        val seenUserRef = FirebaseDatabase.getInstance().reference
                            .child("Seen")
                            .child(story.id)
                            .child(firebaseUser.uid)

                        seenUserRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (!snapshot.exists() && story.timeEnd > System.currentTimeMillis()) {
                                    binding.apply {
                                        storyImage.isVisible = true
                                        storyImageSeen.isVisible = false
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    inner class AddStoryViewHolder(private val binding: AddStoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            binding.apply {
                getUserImage(story.userId)
                checkMyStory()

                root.setOnClickListener {
                    addOrViewMyStory()
                }
            }
        }

        private fun checkMyStory() {
            val userRef = FirebaseDatabase.getInstance().reference
                .child("Stories")
                .child(firebaseUser.uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val activeStoriesCount = snapshot.children.count { storySnapshot ->
                        storySnapshot.getValue(Story::class.java)!!.timeEnd > System.currentTimeMillis()
                    }
                    if (activeStoriesCount > 0) {
                        binding.storyAdd.text = "View story"
                    } else {
                        binding.storyAdd.text = "Add story"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun addOrViewMyStory() {
            val userRef = FirebaseDatabase.getInstance().reference
                .child("Stories")
                .child(firebaseUser.uid)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val activeStoriesCount = snapshot.children.count { storySnapshot ->
                        storySnapshot.getValue(Story::class.java)!!.timeEnd > System.currentTimeMillis()
                    }
                    if (activeStoriesCount > 0) {
                        AlertDialog.Builder(mContext).create().apply {
                            setButton(AlertDialog.BUTTON_POSITIVE, "View story") { _, _ ->
                                val intent =
                                    Intent(mContext, StoryActivity::class.java).apply {
                                        putExtra("userId", firebaseUser.uid)
                                    }
                                mContext.startActivity(intent)
                                dismiss()
                            }
                            setButton(AlertDialog.BUTTON_NEGATIVE, "Add story") { _, _ ->
                                val intent = Intent(mContext, AddStoryActivity::class.java)
                                mContext.startActivity(intent)
                                dismiss()
                            }
                            show()
                        }
                    } else {
                        val intent = Intent(mContext, AddStoryActivity::class.java)
                        mContext.startActivity(intent)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun getUserImage(userId: String) {
            val imageRef = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userId)
                .child("image")

            imageRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val imageUri = snapshot.getValue(String::class.java)
                    Picasso.get().load(imageUri).placeholder(R.drawable.profile)
                        .into(binding.storyImage)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

    }
}
