package com.rsschool.translateapp.presentation.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.rsschool.translateapp.R

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        return
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)
        val aboutPreference = findPreference<Preference>("about")
        aboutPreference?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_Settings_to_About)
            true
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       /* view.findViewById<Toolbar>(R.id.toolbar).apply {
            this.setNavigationOnClickListener {
             //   findNavController().navigate(R.id.action_filterSettingsFragment_to_FirstFragment)
            }
        }*/
    }
}