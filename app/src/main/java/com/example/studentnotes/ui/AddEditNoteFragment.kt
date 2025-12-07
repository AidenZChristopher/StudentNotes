package com.example.studentnotes.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
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
import com.example.studentnotes.BuildConfig
import com.example.studentnotes.R
import com.example.studentnotes.data.entity.Note
import com.example.studentnotes.databinding.FragmentAddednotesBinding
import com.example.studentnotes.viewmodel.NoteViewModel
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@AndroidEntryPoint
class AddEditNoteFragment : Fragment(R.layout.fragment_addednotes) {

    private val viewModel by viewModels<NoteViewModel>()
    private var _binding: FragmentAddednotesBinding? = null
    private val binding get() = _binding!!

    private var currentImagePath: String? = null

    // --- AI MODEL ---
    private val generativeModel by lazy {
        GenerativeModel(
            // CHANGE THIS LINE:
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    // --- ACTIVITY LAUNCHERS ---
    private val speechLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val spokenText: ArrayList<String> =
                    result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                if (spokenText.isNotEmpty()) {
                    val existingText = binding.contentEdit.text.toString()
                    val newText =
                        if (existingText.isBlank()) spokenText[0] else "$existingText\n${spokenText[0]}"
                    binding.contentEdit.setText(newText)
                }
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                currentImagePath = copyImageToInternalStorage(it)
                binding.imagePreview.visibility = View.VISIBLE
                binding.imagePreview.load(File(currentImagePath!!))
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddednotesBinding.bind(view)

        Log.d("API_KEY_CHECK", "API Key from BuildConfig is: ${BuildConfig.GEMINI_API_KEY}")

        val args: AddEditNoteFragmentArgs by navArgs()
        val note = args.note
        val currentFolderId = args.folderId

        setupUI(note)
        setupClickListeners(note, currentFolderId)
        setupMenu()
        observeEvents()
    }

    private fun setupUI(note: Note?) {
        if (note != null) {
            binding.titleEdit.setText(note.title)
            binding.contentEdit.setText(note.content)
            currentImagePath = note.imagePath
            if (currentImagePath != null) {
                binding.imagePreview.visibility = View.VISIBLE
                binding.imagePreview.load(File(currentImagePath!!))
            }
        }
    }

    private fun setupClickListeners(note: Note?, currentFolderId: Int) {
        binding.addImageBtn.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.micBtn.setOnClickListener { startSpeechToText() }
        binding.aiBtn.setOnClickListener {
            val currentText = binding.contentEdit.text.toString()
            if (currentText.isBlank()) {
                Toast.makeText(requireContext(), "Write or speak something first!", Toast.LENGTH_SHORT).show()
            } else {
                summarizeWithAI(currentText)
            }
        }

        binding.saveBtn.setOnClickListener {
            val title = binding.titleEdit.text.toString()
            val content = binding.contentEdit.text.toString()

            if (note != null) { // Update existing note
                val updatedNote = note.copy(
                    title = title,
                    content = content,
                    date = System.currentTimeMillis(),
                    imagePath = currentImagePath
                )
                viewModel.updateNote(updatedNote)
            } else { // Insert new note
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

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notesEvent.collect { event ->
                if (event is NoteViewModel.NotesEvent.NavigateBackWithResult) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupMenu() {
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
                            Toast.makeText(requireContext(), "Cannot share an empty note", Toast.LENGTH_SHORT).show()
                        } else {
                            generatePdfAndShare(title, content)
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Speech recognition not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun summarizeWithAI(text: String) {
        Toast.makeText(requireContext(), "AI is thinking...", Toast.LENGTH_SHORT).show()

        // Hide the button to prevent multiple clicks
        binding.aiBtn.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // More direct prompt for better results
                val prompt = "Refine the grammar of the following text and then create a bulleted summary of the key points:\n\n\"$text\""

                val response = generativeModel.generateContent(prompt)

                response.text?.let { aiOutput ->
                    // Make sure the view is still available before updating
                    if (_binding != null) {
                        val currentContent = binding.contentEdit.text.toString()
                        val newContent = "$currentContent\n\n--- AI Summary ---\n$aiOutput"
                        binding.contentEdit.setText(newContent)
                    }
                }
            } catch (e: Exception) {
                // More specific error logging
                Log.e("AI_ERROR", "Gemini API call failed with exception", e)
                if (_binding != null) { // Check if view is still there to show Toast
                    Toast.makeText(requireContext(), "AI Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } finally {
                // Always re-enable the button, even if the call fails
                if (_binding != null) {
                    binding.aiBtn.isEnabled = true
                }
            }
        }
    }

    private fun generatePdfAndShare(title: String, content: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Draw Title
        paint.color = Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(title, 40f, 60f, paint)

        // Draw Content
        paint.textSize = 14f
        paint.isFakeBoldText = false
        var yPosition = 100f
        val lines = content.split("\n")
        for (line in lines) {
            canvas.drawText(line, 40f, yPosition, paint)
            yPosition += 20f
        }

        pdfDocument.finishPage(page)

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
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Shared Note")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Note via..."))
    }

    private fun copyImageToInternalStorage(uri: Uri): String {
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, fileName)
        requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file.absolutePath
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
