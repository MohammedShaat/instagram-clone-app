package com.example.instagramcloneapp.module

import com.google.firebase.database.PropertyName

data class Notification(
    val id: String = "",
    val text: String = "",
    @set:PropertyName("post_id")
    @get:PropertyName("post_id")
    var postId: String = "",
    @set:PropertyName("user_id")
    @get:PropertyName("user_id")
    var userId: String = "",
    @set:PropertyName("is_post")
    @get:PropertyName("is_post")
    var isPost: Boolean = false,
)
