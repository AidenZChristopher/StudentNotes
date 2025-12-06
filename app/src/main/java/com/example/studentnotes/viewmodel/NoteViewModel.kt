package com.example.studentnotes.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentnotes.data.dao.NoteDao
import com.example.studentnotes.data.entity.Folder
import com.example.studentnotes.data.entity.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(private val dao: NoteDao) : ViewModel() {

    // --- Folder Logic ---
    val folders = dao.getAllFolders()

    fun insertFolder(name: String) = viewModelScope.launch {
        val folder = Folder(name = name)
        dao.insertFolder(folder)
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        dao.deleteFolder(folder)
    }

    // --- Note Logic ---
    private val _currentFolderId = MutableStateFlow(0)

    // Dynamic flow: whenever _currentFolderId changes, this fetches the new list
    val notes = _currentFolderId.flatMapLatest { id ->
        dao.getNotesByFolder(id)
    }

    fun setCurrentFolderId(id: Int) {
        _currentFolderId.value = id
    }

    private val notesChannel = Channel<NotesEvent>()
    val notesEvent = notesChannel.receiveAsFlow()

    fun insert(note: Note) = viewModelScope.launch {
        dao.insertNote(note)
        notesChannel.send(NotesEvent.NavigateBackWithResult(Activity.RESULT_OK))
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        dao.updateNote(note)
        notesChannel.send(NotesEvent.NavigateBackWithResult(Activity.RESULT_OK))
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        dao.deleteNote(note)
        notesChannel.send(NotesEvent.ShowUndoSnackBar("Note Deleted Successfully", note))
    }

    fun onUndoDeleteClick(note: Note) = viewModelScope.launch {
        dao.insertNote(note)
    }

    sealed class NotesEvent {
        data class ShowUndoSnackBar(val msg: String, val note: Note) : NotesEvent()
        data class NavigateBackWithResult(val result: Int) : NotesEvent()
    }
}
