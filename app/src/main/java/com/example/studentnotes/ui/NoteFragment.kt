package com.example.studentnotes.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.studentnotes.R
import com.example.studentnotes.adapter.NoteAdapter
import com.example.studentnotes.data.entity.Note
import com.example.studentnotes.databinding.FragmentNotesBinding
import com.example.studentnotes.viewmodel.NoteViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteFragment : Fragment(R.layout.fragment_notes), NoteAdapter.OnNoteClickListener {

    private val viewModel by viewModels<NoteViewModel>()

    // NEW: Get arguments (folderId, folderName)
    private val args: NoteFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNotesBinding.bind(view)

        // NEW: Set the folder ID in ViewModel to filter notes
        val currentFolderId = args.folderId
        val currentFolderName = args.folderName
        viewModel.setCurrentFolderId(currentFolderId)

        // NEW: Update Toolbar Title to show Folder Name
        (activity as? AppCompatActivity)?.supportActionBar?.title = currentFolderName ?: "Notes"

        binding.apply {
            recyclerViewNotes.layoutManager = GridLayoutManager(context, 2)
            recyclerViewNotes.setHasFixedSize(true)

            addBtn.setOnClickListener {
                // NEW: Pass the folderId when creating a new note
                val action = NoteFragmentDirections.actionNoteFragmentToAddEditNoteFragment(null, currentFolderId)
                findNavController().navigate(action)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.notes.collect { notes ->
                    val adapter = NoteAdapter(notes, this@NoteFragment)
                    recyclerViewNotes.adapter = adapter
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.notesEvent.collect { event: NoteViewModel.NotesEvent ->
                    if (event is NoteViewModel.NotesEvent.ShowUndoSnackBar) {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.note)
                            }.show()
                    }
                }
            }
        }
    }

    override fun onNoteClick(note: Note) {
        // NEW: Pass the folderId (from the note) when editing
        val action = NoteFragmentDirections.actionNoteFragmentToAddEditNoteFragment(note, note.folderId)
        findNavController().navigate(action)
    }

    override fun onNoteLongClick(note: Note) {
        viewModel.deleteNote(note)
    }

    override fun onDeleteClick(note: Note) {
        viewModel.deleteNote(note)
    }
}

