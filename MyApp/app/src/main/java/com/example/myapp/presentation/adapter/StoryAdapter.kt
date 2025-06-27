package com.example.myapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.myapp.R
import com.example.myapp.data.entity.Story
import com.example.myapp.databinding.ItemStoryBinding
import java.io.File


class StoryAdapter(
    private val onItemClick: (Story) -> Unit,
    private val onItemLongClick: (Story) -> Unit
    ) : ListAdapter<Story, StoryAdapter.StoryViewHolder>(StoryDiffCallback()) {

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
            binding.textViewItemStoryTitle.text = story.title
            binding.textViewItemStoryAuthor.text = story.author
            binding.textViewItemStoryGenre.text = story.genre

            if (!story.coverImagePath.isNullOrEmpty()) {
                val coverImageFile = File(story.coverImagePath)
                if (coverImageFile.exists()) {
                    binding.imageViewItemStoryCover.load(coverImageFile) {
                        placeholder(R.drawable.ic_book_placeholder)
                        error(R.drawable.ic_broken_image_placeholder)

                    }
                } else {
                    // File ảnh bìa không tồn tại, hiển thị placeholder
                    binding.imageViewItemStoryCover.load(R.drawable.ic_book_placeholder) {
                        error(R.drawable.ic_broken_image_placeholder)
                    }
                    // Hoặc chỉ set màu nền:
                    // binding.imageViewItemStoryCover.setImageResource(0) // Xóa ảnh cũ
                    // binding.imageViewItemStoryCover.setBackgroundResource(R.color.placeholder_bg)
                }
            } else {
                // Không có đường dẫn ảnh bìa, hiển thị placeholder
                binding.imageViewItemStoryCover.load(R.drawable.ic_book_placeholder) {
                    error(R.drawable.ic_broken_image_placeholder)
                }
                // Hoặc chỉ set màu nền:
                // binding.imageViewItemStoryCover.setImageResource(0) // Xóa ảnh cũ
                // binding.imageViewItemStoryCover.setBackgroundResource(R.color.placeholder_bg)
            }

            binding.root.setOnClickListener { onItemClick(story) }
            binding.root.setOnLongClickListener {
                onItemLongClick(story)
                true
            }
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