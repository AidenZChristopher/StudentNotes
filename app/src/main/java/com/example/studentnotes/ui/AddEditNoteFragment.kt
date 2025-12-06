package com.example.studentnotes.ui

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.studentnotes.R
import com.example.studentnotes.data.entity.Note
import com.example.studentnotes.databinding.FragmentAddednotesBinding
import com.example.studentnotes.viewmodel.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class AddEditNoteFragment : Fragment(R.layout.fragment_addednotes) {

    private val viewModel by viewModels<NoteViewModel>()
    private var currentImagePath: String? = null

    // We need binding accessible for the PDF generator
    private var _binding: FragmentAddednotesBinding? = null
    private val binding get() = _binding!!

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val internalPath = copyImageToInternalStorage(it)
            currentImagePath = internalPath
            binding.imagePreview.visibility = View.VISIBLE
            binding.imagePreview.load(File(internalPath))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddednotesBinding.bind(view)

        val args: AddEditNoteFragmentArgs by navArgs()
        val note = args.note
        val currentFolderId = args.folderId

        // --- MENU SETUP FOR SHARE BUTTON ---
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_add_edit_note, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_share -> {
                        val title = binding.titleEdit.text.toString()
                        val content = binding.contentEdit.text.toString()
                        if (title.isBlank() && content.isBlank()) {
                            Toast.makeText(requireContext(), "Cannot share empty note", Toast.LENGTH_SHORT).show()
                        } else {
                            generatePdfAndShare(title, content)
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        // -----------------------------------

        if (note != null) {
            currentImagePath = note.imagePath
            if (currentImagePath != null) {
                binding.imagePreview.visibility = View.VISIBLE
                binding.imagePreview.load(File(currentImagePath!!))
            }
        }

        binding.addImageBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        if (note != null) {
            binding.apply {
                titleEdit.setText(note.title)
                contentEdit.setText(note.content)
                saveBtn.setOnClickListener {
                    val title = titleEdit.text.toString()
                    val content = contentEdit.text.toString()
                    val updatedNote = note.copy(
                        title = title,
                        content = content,
                        date = System.currentTimeMillis(),
                        imagePath = currentImagePath
                    )
                    viewModel.updateNote(updatedNote)
                }
            }
        } else {
            binding.apply {
                saveBtn.setOnClickListener {
                    val title = titleEdit.text.toString()
                    val content = contentEdit.text.toString()
                    val newNote = Note(
                        title = title,
                        content = content,
                        date = System.currentTimeMillis(),
                        folderId = currentFolderId,
                        imagePath = currentImagePath
                    )
                    viewModel.insert(newNote)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notesEvent.collect { event ->
                if (event is NoteViewModel.NotesEvent.NavigateBackWithResult) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun generatePdfAndShare(title: String, content: String) {
        val pdfDocument = PdfDocument()

        // Create a Page definition (A4 size standard)
        // 595 x 842 is standard A4 at 72 DPI
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // 1. Draw Title
        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(title, 40f, 60f, paint)

        // 2. Draw Content (Simple multiline handling)
        paint.textSize = 14f
        paint.isFakeBoldText = false
        var yPosition = 100f

        // Split content by newlines to handle paragraphs roughly
        val lines = content.split("\n")
        for (line in lines) {
            // Very basic text wrapping could be added here, currently just drawing lines
            canvas.drawText(line, 40f, yPosition, paint)
            yPosition += 20f
        }

        // 3. Draw Image (if exists)
        if (currentImagePath != null) {
            try {
                // Load bitmap (skipping complex scaling logic for brevity)
                // In production, you'd decodeSampledBitmap from path
                // For now, we assume standard usage
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        pdfDocument.finishPage(page)

        // Save to cache directory
        val fileName = "Note_${System.currentTimeMillis()}.pdf"
        val file = File(requireContext().cacheDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            sharePdf(file)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error creating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun sharePdf(file: File) {
        // Create URI using FileProvider
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Shared Note")
            putExtra(Intent.EXTRA_TEXT, "Here is a note I exported from StudentNotes.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share Note via..."))
    }

    private fun copyImageToInternalStorage(uri: Uri): String {
        val context = requireContext()
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "note_image_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
