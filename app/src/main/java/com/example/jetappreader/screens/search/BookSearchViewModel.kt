package com.example.jetappreader.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetappreader.data.Resource
import com.example.jetappreader.model.Item
import com.example.jetappreader.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSearchViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _listOfBooks = MutableStateFlow<Resource<List<Item>>>(Resource.Success(emptyList()))
    val listOfBooks: StateFlow<Resource<List<Item>>> = _listOfBooks.asStateFlow()

    fun searchBooks(query: String) {
        if (query.trim().isEmpty()) return
        viewModelScope.launch {
            _listOfBooks.value = Resource.Loading()
            try {
                val result = repository.getBooksResource(query)
                _listOfBooks.value = result
            } catch (e: Exception) {
                _listOfBooks.value = Resource.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}
