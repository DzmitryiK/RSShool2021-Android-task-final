package com.rsschool.translateapp.presentation.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import com.rsschool.translateapp.R
import com.rsschool.translateapp.databinding.FragmentTranslatorBinding
import com.rsschool.translateapp.domain.util.ResourceState
import com.rsschool.translateapp.presentation.viewmodel.TranslatorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.text.method.ScrollingMovementMethod

import android.R.string.no
import android.text.InputType
import android.view.WindowManager
import android.view.inputmethod.EditorInfo


@AndroidEntryPoint
class TranslatorFragment : Fragment() {

    private var _binding: FragmentTranslatorBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val translatorViewModel: TranslatorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTranslatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Spinners initialization
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arrayListOf<String>())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val srcSpinnerItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                translatorViewModel.onEvent(TranslatorViewModel.TranslatorEvent.SelectSourceLang(position))
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        val resSpinnerItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                translatorViewModel.onEvent(TranslatorViewModel.TranslatorEvent.SelectResultLang(position))
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        with(binding.srcLangSpinner)
        {
            adapter = spinnerAdapter
            setSelection(0, false)
            onItemSelectedListener = null //to not trigger onItemSelected when loading
        }
        with(binding.resLangSpinner)
        {
            adapter = spinnerAdapter
            setSelection(0, false)
            onItemSelectedListener = null
        }

        //Request text set to multiline mode
        binding.textInputName.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.textInputName.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        //Prevent keyboard from resizing layout
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        //Observe state
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    translatorViewModel.languagesListFlow.collect { result ->
                        when(result){
                            is ResourceState.Success -> {
                                binding.srcLangSpinner.isVisible = true
                                binding.resLangSpinner.isVisible = true
                                binding.swapLangsButton.isVisible = true
                                if (result.data != null) {
                                    spinnerAdapter.clear()
                                    spinnerAdapter.addAll(result.data.map {
                                            language -> language.name + " - " + language.code
                                    })
                                }
                            }
                            is ResourceState.Loading -> {
                                binding.srcLangSpinner.isVisible = false
                                binding.resLangSpinner.isVisible = false
                                binding.swapLangsButton.isVisible = false
                            }
                            is ResourceState.Error ->  {
                                binding.srcLangSpinner.isVisible = false
                                binding.resLangSpinner.isVisible = false
                                binding.swapLangsButton.isVisible = false
                            }
                        }
                    }
                }
                launch {
                    translatorViewModel.srcLangFlow.collect{ result ->
                        when(result){
                            is ResourceState.Success -> {
                                if (result.data != null) {
                                    //setSelection triggers onItemSelected
                                    binding.srcLangSpinner.onItemSelectedListener = null
                                    binding.srcLangSpinner.setSelection(result.data.index, false)
                                    binding.srcLangSpinner.onItemSelectedListener = srcSpinnerItemSelectedListener
                                    binding.selectedLangTextView.text =
                                        result.data.name
                                }
                            }
                            is ResourceState.Loading -> {
                                binding.srcLangSpinner.isVisible = false
                            }
                            is ResourceState.Error -> {
                                binding.srcLangSpinner.isVisible = false
                            }
                        }
                    }
                }
                launch {
                    translatorViewModel.resLangFlow.collect{ result ->
                        when(result){
                            is ResourceState.Success -> {
                                if (result.data != null) {
                                    //setSelection triggers onItemSelected
                                    binding.resLangSpinner.onItemSelectedListener = null
                                    binding.resLangSpinner.setSelection(result.data.index, false)
                                    binding.resLangSpinner.onItemSelectedListener = resSpinnerItemSelectedListener
                                    binding.resultLangTextView.text =
                                        result.data.name
                                }
                            }
                            is ResourceState.Loading -> {
                                binding.resLangSpinner.isVisible = false
                            }
                            is ResourceState.Error -> {
                                binding.resLangSpinner.isVisible = false
                            }
                        }
                    }
                }
                launch {
                    translatorViewModel.translateResultFlow.collect { result ->
                        when(result){
                            is ResourceState.Success -> {
                                binding.resProgress.isVisible = false
                                binding.errorLay.isVisible = false
                                binding.resBackground.isVisible = true

                                if (result.data != null) {
                                    binding.resultTextView.text = result.data
                                }
                            }
                            is ResourceState.Loading -> {
                                binding.resBackground.isVisible = false
                                binding.errorLay.isVisible = false
                                binding.resProgress.isVisible = true
                            }
                            is ResourceState.Error -> {
                                binding.resProgress.isVisible = false
                                binding.resBackground.isVisible = false
                                binding.errorText.text = result.message
                                binding.errorLay.isVisible = true

                            }
                        }
                    }
                }
                launch {
                    translatorViewModel.translateQueryFlow.collect{
                        if (it != binding.textInputName.text.toString())
                            binding.textInputName.setText(it)
                    }
                }
                launch {
                    translatorViewModel.currentBookmarkFlow.collect { result ->
                        when(result){
                            is ResourceState.Success -> {
                                val res = result.data
                                if (res != null && res != -1L) {
                                    changeBookmarkIcon(true)
                                }else{
                                    changeBookmarkIcon()
                                }
                            }
                            is ResourceState.Loading -> {
                                changeBookmarkIcon()
                            }
                            is ResourceState.Error -> {
                                changeBookmarkIcon()
                            }
                        }
                    }
                }
                launch {
                    translatorViewModel.eventUIFlow.collect { result ->
                        when(result){
                            is TranslatorViewModel.TranslatorUIEvent.CopyToClipboard -> {
                                hideKeyboard()
                                val clipboard =
                                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Copied Text", result.value)
                                clipboard.setPrimaryClip(clip)

                                Toast.makeText(requireContext(), resources.getString(R.string.clipboard_copied_message), Toast.LENGTH_LONG).show()
                            }
                            is TranslatorViewModel.TranslatorUIEvent.Share -> {
                                hideKeyboard()
                                val prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
                                val sharedText = when(prefs.getString("sharePref",resources.getString(R.string.share_option_default))){
                                    resources.getString(R.string.share_option_default) ->
                                        result.result
                                    resources.getString(R.string.share_option_pair) ->
                                        "${result.query} - ${result.result}"
                                    resources.getString(R.string.share_option_pair_more) ->
                                        "${result.query} (${result.sourceLang}) - ${result.result} (${result.resultLang})"
                                    else -> {result.result}
                                }

                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, sharedText)
                                    type = "text/plain"
                                }

                                val shareIntent = Intent.createChooser(sendIntent, null)
                                startActivity(shareIntent)
                            }
                            is TranslatorViewModel.TranslatorUIEvent.ShowToast -> {
                                Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }

        //Notifying about user actions
        binding.swapLangsButton.setOnClickListener {
            translatorViewModel.onEvent(TranslatorViewModel.TranslatorEvent.SwapLanguages)
        }
        binding.textInputName.addTextChangedListener {
            translatorViewModel.onEvent(TranslatorViewModel.TranslatorEvent.TextChanged(it.toString()))
        }
        binding.textInputName.setOnEditorActionListener { textView, i, _ ->
            hideKeyboard()
            translatorViewModel.onEvent(TranslatorViewModel.TranslatorEvent.TextEnter)
            return@setOnEditorActionListener true
        }
        binding.textInputLayName.setEndIconOnClickListener {
            binding.textInputName.setText("")
            translatorViewModel.onEvent(TranslatorViewModel.TranslatorEvent.ClearText)
        }
        binding.clipboardButton.setOnClickListener {
            translatorViewModel.onEvent(TranslatorViewModel.TranslatorEvent.CopyToClipboard)
        }
        binding.bookmarkButton.setOnClickListener {
            hideKeyboard()
            translatorViewModel.onEvent(TranslatorViewModel.TranslatorEvent.Bookmark)
        }
        binding.shareButton.setOnClickListener {
            translatorViewModel.onEvent(TranslatorViewModel.TranslatorEvent.Share)
        }
        translatorViewModel.onEvent(TranslatorViewModel.TranslatorEvent.CheckBookmark)
    }

    private fun hideKeyboard(){
        val inputMethodManager = ContextCompat.getSystemService(
            requireContext(),
            InputMethodManager::class.java
        ) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.textInputName.windowToken, 0)
    }

    private fun changeBookmarkIcon(filled:Boolean = false){
        if (filled){
            binding.bookmarkButton.setImageResource(R.drawable.ic_baseline_star_24)
        }else{
            binding.bookmarkButton.setImageResource(R.drawable.ic_baseline_star_border_24)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}