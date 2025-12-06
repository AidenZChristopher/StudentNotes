package com.example.studentnotes.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentnotes.R
import com.example.studentnotes.adapter.FolderAdapter
import com.example.studentnotes.data.entity.Folder
import com.example.studentnotes.databinding.FragmentFoldersBinding
import com.example.studentnotes.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FolderFragment : Fragment(R.layout.fragment_folders), FolderAdapter.OnFolderClickListener {

    private val viewModel by viewModels<NoteViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentFoldersBinding.bind(view)
        val folderAdapter = FolderAdapter(this)

        binding.recyclerViewFolders.apply {
            adapter = folderAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.addFolderBtn.setOnClickListener {
            showAddFolderDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.folders.collect { folders ->
                folderAdapter.submitList(folders)
            }
        }

        // --- NEW: Search Menu Setup ---
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_search, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = true
                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.searchQuery.value = newText.orEmpty()
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    // ... existing showAddFolderDialog, onFolderClick, onFolderDelete methods ...
    private fun showAddFolderDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("New Folder")
        val input = EditText(requireContext())
        input.hint = "Enter folder name"
        builder.setView(input)

        builder.setPositiveButton("Create") { _, _ ->
            val folderName = input.text.toString()
            if (folderName.isNotEmpty()) {
                viewModel.insertFolder(folderName)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    override fun onFolderClick(folder: Folder) {
        val action = FolderFragmentDirections.actionFolderFragmentToNoteFragment(folder.id, folder.name)
        findNavController().navigate(action)
    }

    override fun onFolderDelete(folder: Folder) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Folder")
            .setMessage("Are you sure? All notes inside will be deleted.")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteFolder(folder)
            }
            .setNegativeButton("No", null)
            .show()
    }
}
