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

        val args: AddEditNoteFragmentArgs by navArgs()
        val note = args.note
        val currentFolderId = args.folderId

        if (note != null) {
            binding.apply {
                titleEdit.setText(note.title)
                contentEdit.setText(note.content)

                saveBtn.setOnClickListener {
                    val title = titleEdit.text.toString()
                    val content = contentEdit.text.toString()

                    // Copy preserves the existing folderId and ID
                    val updateNote = note.copy(
                        title = title,
                        content = content,
                        date = System.currentTimeMillis()
                    )
                    viewModel.updateNote(updateNote)
                }
            }
        } else {
            // CREATE NEW NOTE
            binding.apply {
                saveBtn.setOnClickListener {
                    val title = titleEdit.text.toString()
                    val content = contentEdit.text.toString()

                    // Must include folderId here so the note goes into the correct folder
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notesEvent.collect { event: NoteViewModel.NotesEvent ->
                if (event is NoteViewModel.NotesEvent.NavigateBackWithResult) {
                    // Correctly go back to the previous screen (NoteFragment)
                    findNavController().popBackStack()
                }
            }
        }
    }
}
