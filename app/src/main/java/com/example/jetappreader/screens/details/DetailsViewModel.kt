package com.example.jetappreader.screens.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetappreader.data.Resource
import com.example.jetappreader.model.Item
import com.example.jetappreader.model.MBook
import com.example.jetappreader.repository.BookRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _bookInfo = MutableStateFlow<Resource<Item>>(Resource.Loading())
    val bookInfo: StateFlow<Resource<Item>> = _bookInfo.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    fun getBookInfo(bookId: String) {
        if (bookId.isEmpty()) return
        viewModelScope.launch {
            _bookInfo.value = Resource.Loading()
            try {
                val result = bookRepository.getBookInfoResource(bookId)
                _bookInfo.value = result
            } catch (e: Exception) {
                _bookInfo.value = Resource.Error(e.message ?: "Failed to fetch book details")
            }
        }
    }

    fun saveBookToFirestore(
        book: MBook,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (_saving.value) return

        val currentUser = try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Exception) {
            null
        }

        if (currentUser == null) {
            onError("User not logged in. Please sign in.")
            return
        }

        book.userId = currentUser.uid
        _saving.value = true

        try {
            val docRef = firestore.collection("books").document()
            val docId = docRef.id
            book.id = docId

            docRef.set(book.toMap())
                .addOnFailureListener { e ->
                    Log.e("DetailsViewModel", "Error syncing book to Firestore server", e)
                }

            _saving.value = false
            onSuccess()
        } catch (e: Exception) {
            _saving.value = false
            Log.e("DetailsViewModel", "Error saving book", e)
            onError(e.localizedMessage ?: "Failed to save book.")
        }
    }
}
