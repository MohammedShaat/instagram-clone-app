package com.example.instagramcloneapp.module

data class Post(
    val id: String,
    val description: String,
    val image: String,
    val publisher: String,
) {
    constructor() : this("", "", "", "")
}