package com.example.instagramcloneapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.databinding.ImageItemBinding
import com.example.instagramcloneapp.fragments.PostDetailsFragment
import com.example.instagramcloneapp.module.Post
import com.squareup.picasso.Picasso

class MyImageAdapter(private val mContext: Context, private val mPosts: List<Post>) :
    RecyclerView.Adapter<MyImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mPosts[position])
    }

    override fun getItemCount(): Int {
        return mPosts.size
    }

    inner class ViewHolder(private val binding: ImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                Picasso.get().load(post.image).placeholder(R.drawable.profile).into(postImage)

                root.setOnClickListener {
                    val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                    pref.putString("postId", post.id)
                    pref.apply()
                    (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PostDetailsFragment()).commit()
                }
            }
        }
    }
}