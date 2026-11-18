package com.example.jetappreader.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetappreader.data.DataOrException
import com.example.jetappreader.model.MBook
import com.example.jetappreader.repository.FireRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val repository: FireRepository
) : ViewModel() {

    private val _data = MutableStateFlow<DataOrException<List<MBook>, Boolean, Exception>>(
        DataOrException(data = emptyList(), loading = true, e = null)
    )
    val data: StateFlow<DataOrException<List<MBook>, Boolean, Exception>> = _data.asStateFlow()

    init {
        getAllBooksFromDatabase()
    }

    fun getAllBooksFromDatabase() {
        viewModelScope.launch {
            _data.value = _data.value.copy(loading = true)
            _data.value = repository.getAllBooksFromDatabase()
        }
    }
}
