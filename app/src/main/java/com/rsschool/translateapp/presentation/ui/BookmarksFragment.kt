package com.rsschool.translateapp.presentation.ui

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.rsschool.translateapp.R
import com.rsschool.translateapp.databinding.FragmentBookmarksBinding
import com.rsschool.translateapp.domain.model.Bookmark
import com.rsschool.translateapp.domain.util.ResourceState
import com.rsschool.translateapp.presentation.util.BookmarksListAdapter
import com.rsschool.translateapp.presentation.util.BookmarksListener
import com.rsschool.translateapp.presentation.viewmodel.BookmarksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookmarksFragment : Fragment(), BookmarksListener {

    private var _binding: FragmentBookmarksBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val bookmarksViewModel: BookmarksViewModel by viewModels()
    private lateinit var adapterBookmarks: BookmarksListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        menu.add(Menu.NONE, 101, Menu.NONE, "Refresh")
            .setIcon(R.drawable.ic_baseline_refresh_24)
            .setShowAsAction(1)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            101 -> {bookmarksViewModel.onEvent(BookmarksViewModel.BookmarksEvent.RefreshItems)
                true}
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //RecyclerView init
        adapterBookmarks = BookmarksListAdapter(this)
        binding.bookmarksRecycler.layoutManager = LinearLayoutManager(context)
        binding.bookmarksRecycler.adapter = adapterBookmarks

        //Observe state
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    bookmarksViewModel.bookmarksFlow.collect { result ->
                        binding.noBookmarksTextView.text =
                            resources.getString(R.string.bookmarks_empty)
                        when (result){
                            is ResourceState.Success ->{
                                binding.resProgress.isVisible = false
                                binding.noBookmarksTextView.isVisible = result.data!!.isEmpty()
                                adapterBookmarks.submitList(result.data)
                            } is ResourceState.Loading ->{
                                binding.resProgress.isVisible = true
                            } is ResourceState.Error ->{
                            binding.resProgress.isVisible = false
                            binding.noBookmarksTextView.text =
                                resources.getString(R.string.bookmarks_error)
                            binding.noBookmarksTextView.isVisible = true
                            }
                        }

                    }
                }
                launch {
                    bookmarksViewModel.eventUIFlow.collect { result ->
                        when(result){
                            is BookmarksViewModel.BookmarksUIEvent.ShowToast -> {
                                Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun bookmarkItem(bookmark: Bookmark) {
        bookmarksViewModel.onEvent(BookmarksViewModel.BookmarksEvent.BookmarkItem(bookmark))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}