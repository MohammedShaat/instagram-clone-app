package com.example.instagramcloneapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.instagramcloneapp.databinding.ActivityStoryBinding
import com.example.instagramcloneapp.module.Story
import com.example.instagramcloneapp.module.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView

class StoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryBinding

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var userId: String
    private val storyList: MutableList<Story> = mutableListOf()
    private var activeStoryIndex = 0
    private lateinit var storiesProgressView: StoriesProgressView
    private lateinit var imageStory: ImageView

    private var pressTime = 0L
    private var limit = 500L

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        userId = requireNotNull(intent.getStringExtra("userId"))

        imageStory = binding.imageStory
        storiesProgressView = binding.stories

        binding.linearLayoutSeen.isVisible = firebaseUser.uid == userId
        binding.storyDeleteBtn.isVisible = firebaseUser.uid == userId

        getUserInfo()
        getStories()

        val onTouchListener = View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    pressTime = System.currentTimeMillis()
                    storiesProgressView.pause()
                    false
                }

                MotionEvent.ACTION_UP -> {
                    pressTime = System.currentTimeMillis() - pressTime
                    storiesProgressView.resume()
                    limit < pressTime
                }

                else -> false
            }
        }

        binding.reverse.apply {
            setOnClickListener { storiesProgressView.reverse() }
            setOnTouchListener(onTouchListener)
        }


        binding.skip.apply {
            setOnClickListener { storiesProgressView.skip() }
            setOnTouchListener(onTouchListener)
        }


        binding.storyDeleteBtn.setOnClickListener {
            deleteStory()
        }

        binding.linearLayoutSeen.setOnClickListener {
            val intent = Intent(this, ShowUsersActivity::class.java).apply {
                putExtra("title", "views")
                putExtra("storyId", storyList[activeStoryIndex].id)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        storiesProgressView.resume()
    }

    override fun onPause() {
        super.onPause()
        storiesProgressView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        storiesProgressView.destroy()
    }

    private fun getStories() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Stories")
            .child(userId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                storiesProgressView.pause()
                storyList.clear()
                snapshot.children.forEach { storySnapshot ->
                    val story = storySnapshot.getValue(Story::class.java)!!
                    if (story.timeEnd > System.currentTimeMillis())
                        storyList.add(story)
                }

                if (storyList.isEmpty()) return

                activeStoryIndex = 0

                Picasso.get().load(storyList[activeStoryIndex].imageUrl)
                    .placeholder(R.drawable.profile)
                    .into(imageStory)

                seeStory(storyList[activeStoryIndex].id)
                getSeenNumber(storyList[activeStoryIndex].id)

                storiesProgressView.apply {
                    setStoriesCount(storyList.size)
                    setStoryDuration(3000)
                    setStoriesListener(object : StoriesProgressView.StoriesListener {
                        override fun onNext() {
                            activeStoryIndex++
                            if (activeStoryIndex !in storyList.indices) return

                            seeStory(storyList[activeStoryIndex].id)
                            getSeenNumber(storyList[activeStoryIndex].id)

                            Picasso.get().load(storyList[activeStoryIndex].imageUrl)
                                .placeholder(R.drawable.profile)
                                .into(imageStory)
                        }

                        override fun onPrev() {
                            activeStoryIndex--
                            if (activeStoryIndex !in storyList.indices) return

                            seeStory(storyList[activeStoryIndex].id)
                            getSeenNumber(storyList[activeStoryIndex].id)

                            Picasso.get().load(storyList[activeStoryIndex].imageUrl)
                                .placeholder(R.drawable.profile)
                                .into(imageStory)
                        }

                        override fun onComplete() {
                            finish()
                        }
                    })
                    startStories(activeStoryIndex)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getSeenNumber(storyId: String) {
        val storyRef = FirebaseDatabase.getInstance().reference
            .child("Seen")
            .child(storyId)

        storyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.seenNumber.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun seeStory(storyId: String) {
        if (firebaseUser.uid != userId)
            FirebaseDatabase.getInstance().reference
                .child("Seen")
                .child(storyId)
                .child(firebaseUser.uid)
                .setValue(true)
    }

    private fun deleteStory() {
        FirebaseDatabase.getInstance().reference
            .child("Stories")
            .child(firebaseUser.uid)
            .child(storyList[activeStoryIndex].id)
            .removeValue()
    }

    private fun getUserInfo() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val user = snapshot.getValue(User::class.java)!!
                binding.apply {
                    storyUserName.text = user.fullname
                    Picasso.get().load(user.image).placeholder(R.drawable.profile)
                        .into(storyProfileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
