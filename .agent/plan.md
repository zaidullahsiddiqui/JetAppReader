# Project Plan

The "App Reader" capstone project is a comprehensive application that allows users to manage their reading experience effectively. Here's a summary of its main features and functionalities:

User Authentication: Users can sign up and log in to the app (Firebase/custom auth), enabling personalized access to their book collections.

Book Management: Users can add books to their collection, update their status (e.g., currently reading, want to read, finished reading), or remove them as needed.

Search Functionality: Utilizing the Google Books API, the app allows users to search for books on various topics, enhancing the ability to discover new reading material.

User Interface: The application features a clean and engaging Jetpack Compose interface, complete with smooth animations that improve user experience.

Data Fetching & Architecture: Retrofit HTTP client for retrieving book information from Google Books API, Dagger/Hilt for Dependency Injection, Firebase Firestore for saving user books/reading list data, Repository pattern, ViewModel clean architecture.

## Project Brief

# App Reader - Project Brief

## Features

1. **User Authentication**: Secure sign-up and log-in functionality powered by Firebase Authentication, giving users personalized access to their reading collection.
2. **Book Search & Discovery**: Search for books across various topics, titles, and authors utilizing the Google Books API.
3. **Personal Library & Reading Status Management**: Add books to a personal library, track reading progress by updating status (*Want to Read*, *Currently Reading*, *Finished*), and manage book collections backed by Firebase Firestore.
4. **Adaptive Compose UI**: Modern declarative UI built with Jetpack Compose featuring smooth animations and responsive multi-pane support.

## High-Level Technical Stack

- **Language & Concurrency**: Kotlin, Kotlin Coroutines & Flow
- **UI & Layouts**: Jetpack Compose, Material 3
- **Navigation & Adaptive Strategy**: Jetpack Navigation 3 (state-driven) and Compose Material Adaptive library
- **Architecture**: Clean Architecture / MVVM with `ViewModel` and Repository pattern
- **Dependency Injection**: Hilt
- **Networking**: Retrofit & OkHttp (Google Books API integration)
- **Backend & Cloud Persistence**: Firebase Authentication & Firebase Firestore

## Implementation Steps
**Total Duration:** 47h 18m 13s

### Task_1_SetupDataAndNetwork: Set up Hilt DI, Retrofit client for Google Books API, and Firebase Authentication & Firestore repositories.
- **Status:** COMPLETED
- **Updates:** Set up Hilt DI, Retrofit client for Google Books API (BookApi, BookRepository), Firestore repository (FireRepository), data models (Book, MBook, Item, etc.), and verified debug build passes cleanly with ./gradlew assembleDebug.
- **Acceptance Criteria:**
  - Build pass
  - Retrofit Google Books API client configured with API_KEY integration
  - Firebase Auth and Firestore repositories created and wired with Hilt
- **Duration:** 4h 33m 58s

### Task_2_AuthAndNavigation: Implement authentication screens (Login / Register) and app navigation routing.
- **Status:** COMPLETED
- **Updates:** Login, Create Account, Splash, and ReaderNavigation routing implemented and integrated with Firebase Auth. Verified build passing cleanly with assembleDebug.
- **Acceptance Criteria:**
  - Login and Register screens built using Jetpack Compose
  - Firebase Auth integrated into UI flows
  - Navigation routing configured for auth and main app screens
- **Duration:** 42h 35m 21s

### Task_3_LibraryAndSearchUI: Implement Home/Library, Book Search, and Book Details/Status screens with Firestore sync and Google Books API integration.
- **Status:** COMPLETED
- **Updates:** HomeScreen, BookSearchScreen, BookDetailsScreen, BookUpdateScreen, and ReaderStatsScreen fully implemented with Google Books API integration via Retrofit and Firestore synchronization. Navigation routes and ViewModels wired correctly. Verified build passing cleanly with assembleDebug.
- **Acceptance Criteria:**
  - Home screen displays books from Firestore library
  - Search screen executes query via Google Books API
  - Book Details screen enables updating reading status ('Want to Read', 'Currently Reading', 'Finished') in Firestore
- **Duration:** 8m 54s

### Task_4_RunAndVerify: Final build, execution, and verification of application stability and requirements.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - build pass
  - make sure all existing tests pass
  - app does not crash
  - critic_agent verifies application stability (no crashes), confirms alignment with user requirements, and reports critical UI issues
- **StartTime:** 2026-11-11 18:21:13 PKT

