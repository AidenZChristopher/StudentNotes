package com.example.studentnotes.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studentnotes.data.entity.Note
import com.example.studentnotes.databinding.ItemNotesBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import coil.load
import java.io.File
import android.view.View

class NoteAdapter(
    private val mNotes: List<Note>,
    private val listener: OnNoteClickListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    interface OnNoteClickListener {
        fun onNoteClick(note: Note)
        fun onNoteLongClick(note: Note)
        fun onDeleteClick(note: Note) // NEW METHOD
    }

    inner class NoteViewHolder(private val binding: ItemNotesBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                // Click on the whole card to edit
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val note = mNotes[position]
                        listener.onNoteClick(note)
                    }
                }

                // Keep long click if you want, or remove it
                root.setOnLongClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val note = mNotes[position]
                        listener.onNoteLongClick(note)
                    }
                    true
                }

                // NEW: Click on the delete button
                deleteBtn.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val note = mNotes[position]
                        listener.onDeleteClick(note)
                    }
                }
            }
        }

        fun bind(note: Note) {
            binding.apply {
                titleNote.text = note.title
                contentNote.text = note.content
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateNotes.text = formatter.format(Date(note.date))

                if (note.imagePath != null) {
                    noteImage.visibility = View.VISIBLE
                    noteImage.load(File(note.imagePath))
                } else {
                    noteImage.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNotesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(mNotes[position])
    }

    override fun getItemCount(): Int {
        return mNotes.size
    }
}
