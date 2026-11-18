package com.example.jetappreader.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetappreader.model.MUser
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
class LoginScreenViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        home: () -> Unit
    ) {
        if (_loading.value) return
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                home()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Authentication failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        home: () -> Unit
    ) {
        if (_loading.value) return
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                val displayName = email.split('@')[0]
                val userId = user?.uid ?: ""
                createFirebaseUser(userId, displayName, home)
            } catch (e: Exception) {
                _loading.value = false
                _error.value = e.localizedMessage ?: "User registration failed"
            }
        }
    }

    private fun createFirebaseUser(
        userId: String,
        displayName: String,
        home: () -> Unit
    ) {
        val user = MUser(
            id = null,
            userId = userId,
            displayName = displayName,
            avatarUrl = "",
            quote = "Life is short, read books",
            profession = "Reader"
        )
        viewModelScope.launch {
            try {
                val docRef = firestore.collection("users").document()
                val userWithId = user.copy(id = docRef.id)
                docRef.set(userWithId).await()
            } catch (e: Exception) {
                Log.d("LoginViewModel", "Error saving user to Firestore: ${e.localizedMessage}")
            } finally {
                _loading.value = false
                home()
            }
        }
    }
}
