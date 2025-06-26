package com.example.myapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.data.entity.Story
import com.example.myapp.databinding.ItemStoryBinding

class StoryAdapter(private val onItemClick: (Story) -> Unit) :
    ListAdapter<Story, StoryAdapter.StoryViewHolder>(StoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        holder.bind(story)
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            binding.storyTitleTextView.text = story.title
            binding.storyAuthorTextView.text = story.author
            binding.root.setOnClickListener { onItemClick(story) }
        }
    }
}

class StoryDiffCallback : DiffUtil.ItemCallback<Story>() {
    override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
        return oldItem == newItem
    }
}