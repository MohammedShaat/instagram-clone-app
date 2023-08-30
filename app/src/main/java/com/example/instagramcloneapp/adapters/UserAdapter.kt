package com.example.instagramcloneapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.databinding.UserItemLayoutBinding
import com.example.instagramcloneapp.module.User
import com.squareup.picasso.Picasso

class UserAdapter(
    private val mContext: Context,
    private val mUser: List<User>,
    private val isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = UserItemLayoutBinding.inflate(LayoutInflater.from(mContext))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUser[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    class ViewHolder(private val binding: UserItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                userFullNameSearch.text = user.fullname
                userNameSearch.text = user.username
                Picasso.get().load(user.image).placeholder(R.drawable.profile)
                    .into(userProfileImage)
            }
        }
    }
}