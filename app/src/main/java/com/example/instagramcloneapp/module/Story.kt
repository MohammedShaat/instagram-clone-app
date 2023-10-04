package com.example.instagramcloneapp.module

import com.google.firebase.database.PropertyName

data class Story(
    val id: String = "",

    @set:PropertyName("image_url")
    @get:PropertyName("image_url")
    var imageUrl: String = "",

    @set:PropertyName("time_start")
    @get:PropertyName("time_start")
    var timeStart: Long = 0,

    @set:PropertyName("time_end")
    @get:PropertyName("time_end")
    var timeEnd: Long = 0,

    @set:PropertyName("user_id")
    @get:PropertyName("user_id")
    var userId: String = "",
)
