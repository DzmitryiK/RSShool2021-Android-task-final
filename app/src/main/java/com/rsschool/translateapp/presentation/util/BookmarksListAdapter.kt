package com.rsschool.translateapp.presentation.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rsschool.translateapp.R
import com.rsschool.translateapp.databinding.BookmarkItemBinding
import com.rsschool.translateapp.presentation.util.BookmarksListAdapter.BookmarkViewHolder
import com.rsschool.translateapp.domain.model.Bookmark

class BookmarksListAdapter(private val listener: BookmarksListener):
    ListAdapter<Bookmark, BookmarkViewHolder>(BOOKMARKS_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = BookmarkItemBinding.inflate(layoutInflater, parent, false)
        return BookmarkViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    override fun onBindViewHolder(
        holder: BookmarkViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNullOrEmpty()){
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder.bind(currentList[position])
        }

    }

    class BookmarkViewHolder(private val binding: BookmarkItemBinding,
                           private val listener: BookmarksListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bookmark: Bookmark) {
            binding.srcLangName.text = bookmark.sourceLang
            binding.srcWord.text = bookmark.sourceText
            binding.resLangName.text = bookmark.resultLang
            binding.resWord.text = bookmark.resultText

            if (bookmark.view_id >= 0){
                binding.bookmarkButton.setImageResource(R.drawable.ic_baseline_star_24)
            }else{
                binding.bookmarkButton.setImageResource(R.drawable.ic_baseline_star_border_24)
            }

           binding.bookmarkButton.setOnClickListener {
               listener.bookmarkItem(bookmark)
           }
        }
    }

    companion object {
        private val BOOKMARKS_COMPARATOR = object : DiffUtil.ItemCallback<Bookmark>() {

            //todo: blinking after delete bookmark
            override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
                return oldItem.sourceLang == newItem.sourceLang &&
                        oldItem.sourceText == newItem.sourceText &&
                        oldItem.resultLang == newItem.resultLang &&
                        oldItem.resultText == newItem.resultText &&
                        oldItem.view_id == newItem.view_id
            }

            override fun getChangePayload(oldItem: Bookmark, newItem: Bookmark): Any? {
                if (oldItem.view_id == -1L && newItem.view_id != -1L) {
                        return newItem.view_id
                } else if
                    (newItem.view_id == -1L && oldItem.view_id != -1L)
                        return oldItem.view_id
                return super.getChangePayload(oldItem, newItem)
            }
        }
    }
}