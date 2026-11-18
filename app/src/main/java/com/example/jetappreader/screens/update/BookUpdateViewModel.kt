package com.example.jetappreader.screens.update

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetappreader.data.Resource
import com.example.jetappreader.model.MBook
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class BookUpdateViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _bookState = MutableStateFlow<Resource<MBook>>(Resource.Loading())
    val bookState: StateFlow<Resource<MBook>> = _bookState.asStateFlow()

    private val _updating = MutableStateFlow(false)
    val updating: StateFlow<Boolean> = _updating.asStateFlow()

    fun getBook(bookId: String) {
        if (bookId.isEmpty()) return
        viewModelScope.launch {
            _bookState.value = Resource.Loading()
            try {
                val snapshot = firestore.collection("books")
                    .document(bookId)
                    .get()
                    .await()
                if (snapshot.exists()) {
                    val book = snapshot.toObject(MBook::class.java)?.apply {
                        if (id.isNullOrEmpty()) id = snapshot.id
                    }
                    if (book != null) {
                        _bookState.value = Resource.Success(book)
                    } else {
                        findBookByGoogleId(bookId)
                    }
                } else {
                    findBookByGoogleId(bookId)
                }
            } catch (_: Exception) {
                findBookByGoogleId(bookId)
            }
        }
    }

    private fun findBookByGoogleId(googleBookId: String) {
        viewModelScope.launch {
            try {
                val querySnapshot = firestore.collection("books")
                    .whereEqualTo("google_book_id", googleBookId)
                    .get()
                    .await()
                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents.first()
                    val book = doc.toObject(MBook::class.java)?.apply {
                        if (id.isNullOrEmpty()) id = doc.id
                    }
                    if (book != null) {
                        _bookState.value = Resource.Success(book)
                    } else {
                        _bookState.value = Resource.Error("Book not found in database.")
                    }
                } else {
                    _bookState.value = Resource.Error("Book not found in database.")
                }
            } catch (e: Exception) {
                _bookState.value = Resource.Error(e.localizedMessage ?: "Error fetching book.")
            }
        }
    }

    fun updateBook(
        docId: String,
        updateData: Map<String, Any?>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (_updating.value) return

        val currentUser = try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Exception) {
            null
        }

        if (currentUser == null) {
            onError("User not logged in.")
            return
        }

        if (docId.isEmpty()) {
            onError("Book ID is empty. Cannot update.")
            return
        }

        _updating.value = true

        try {
            firestore.collection("books")
                .document(docId)
                .update(updateData)
                .addOnFailureListener { e ->
                    Log.e("BookUpdateViewModel", "Error updating book on server", e)
                }

            _updating.value = false
            onSuccess()
        } catch (e: Exception) {
            _updating.value = false
            Log.e("BookUpdateViewModel", "Error updating book", e)
            onError(e.localizedMessage ?: "Failed to update book.")
        }
    }

    fun deleteBook(
        docId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (_updating.value) return

        val currentUser = try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Exception) {
            null
        }

        if (currentUser == null) {
            onError("User not logged in.")
            return
        }

        if (docId.isEmpty()) {
            onError("Book ID is empty. Cannot delete.")
            return
        }

        _updating.value = true

        try {
            firestore.collection("books")
                .document(docId)
                .delete()
                .addOnFailureListener { e ->
                    Log.e("BookUpdateViewModel", "Error deleting book from server", e)
                }

            _updating.value = false
            onSuccess()
        } catch (e: Exception) {
            _updating.value = false
            Log.e("BookUpdateViewModel", "Error deleting book", e)
            onError(e.localizedMessage ?: "Failed to delete book.")
        }
    }
}
