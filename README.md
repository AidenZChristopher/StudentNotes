-- Overview --

Student Notes is a android application built on Android 5.0 Lollipop using Kotlin. The user is able to create folders to organize notes. To put content in the note the user can upload images, use text to speech, or type. After the note is completed they can save it to their device or share and export.

-- Key Features --

Folder Creation
- Organize Notes using Folders

Search Function
- Search folders / notes for specific keywords

Speach to Text
- Speak into mic and text is automatically transcribed.

Image Upload
- Upload images into Notes

Delete Function
- Delete unused or accidental folders / notes

-- Technical Components --
Architecture
- MVVM (Model-View-ViewModel)
- Dependency Injection: Hilt for compile-time dependency injection and managing ViewModel lifecycles.
- Kotlin Coroutines for background operations (database I/O, API calls)
- Reactive Data: Kotlin Flow (StateFlow, flatMapLatest, combine) for reactive UI updates, search filtering, and folder navigation.

User Interface
- XML uses standard layouts including ConstraintLayout and CoordinatorLayout.
- Data Binding: Used to bind UI components directly to data sources, reducing boilerplate code.
- Navigation Component: Single-Activity architecture using NavHostFragment and Safe Args for type-safe argument passing between screens.
- Integrated SearchView with real-time filtering of database results.

Media & File Handling
- Image Loading: Coil for memory-efficient image loading and caching.
- Internal Storage: Custom logic to copy user-selected images from the gallery to the app's private storage to ensure persistence.
- Speech-to-Text: Integration with Android's native RecognizerIntent for voice dictation.•PDF Generation: Native PdfDocument and Canvas drawing APIs to generate reports from note content.
- File Sharing: configured FileProvider to securely share generated PDFs with external apps (Gmail, Drive, etc.).

-- Demo and Screenshots --


https://github.com/user-attachments/assets/02ebfee8-bf45-4e82-9372-c8561c9bdab2


<img width="398" height="818" alt="Screenshot 2025-12-08 163741" src="https://github.com/user-attachments/assets/16213bcc-1d06-4b5f-9f17-60058ee9fdcf" />
<img width="398" height="818" alt="Screenshot 2025-12-08 163741" src="https://github.com/user-attachments/assets/9e64657a-2932-4067-a6f2-36f5157afd05" />
<img width="392" height="824" alt="Screenshot 2025-12-08 163810" src="https://github.com/user-attachments/assets/035ab9d8-d6ba-4dd5-a3d7-9c10c51dbce8" />
<img width="421" height="828" alt="Screenshot 2025-12-08 163828" src="https://github.com/user-attachments/assets/1881d7b6-3547-4e76-9dea-bbf91835d685" />
<img width="403" height="821" alt="Screenshot 2025-12-08 170519" src="https://github.com/user-attachments/assets/c8a6c7a2-a069-449f-9861-4a543b2f586b" />
<img width="396" height="829" alt="Screenshot 2025-12-08 163838" src="https://github.com/user-attachments/assets/aed4b815-a2de-40f7-bea4-4173a42752a9" />
