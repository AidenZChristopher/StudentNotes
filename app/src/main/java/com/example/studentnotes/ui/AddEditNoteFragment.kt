package com.example.studentnotes.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.studentnotes.R
import com.example.studentnotes.data.entity.Note
import com.example.studentnotes.databinding.FragmentAddednotesBinding
import com.example.studentnotes.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditNoteFragment : Fragment(R.layout.fragment_addednotes) {

    private val viewModel by viewModels<NoteViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddednotesBinding.bind(view)

        // These arguments come from nav_graph.xml
        val args: AddEditNoteFragmentArgs by navArgs()
        val note = args.note
        val currentFolderId = args.folderId

        // NOTE: Manual back button listener removed.
        // The MainActivity setupActionBarWithNavController handles the back arrow now.

        if (note != null) {
            // --- EDITING AN EXISTING NOTE ---
            binding.apply {
                titleEdit.setText(note.title)
                contentEdit.setText(note.content)

                saveBtn.setOnClickListener {
                    val title = titleEdit.text.toString()
                    val content = contentEdit.text.toString()

                    val updatedNote = note.copy(
                        title = title,
                        content = content,
                        date = System.currentTimeMillis()
                    )
                    viewModel.updateNote(updatedNote)
                }
            }
        } else {
            // --- CREATING A NEW NOTE ---
            binding.apply {
                saveBtn.setOnClickListener {
                    val title = titleEdit.text.toString()
                    val content = contentEdit.text.toString()

                    val newNote = Note(
                        title = title,
                        content = content,
                        date = System.currentTimeMillis(),
                        folderId = currentFolderId
                    )

                    viewModel.insert(newNote)
                }
            }
        }

        // --- HANDLE NAVIGATION EVENTS FROM VIEWMODEL ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notesEvent.collect { event ->
                if (event is NoteViewModel.NotesEvent.NavigateBackWithResult) {
                    // Go back to the previous screen (NoteFragment)
                    findNavController().popBackStack()
                }
            }
        }
    }
}
