package com.example.jetappreader.repository

import com.example.jetappreader.data.DataOrException
import com.example.jetappreader.model.MBook
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FireRepository @Inject constructor(
    private val queryBook: Query
) {
    suspend fun getAllBooksFromDatabase(): DataOrException<List<MBook>, Boolean, Exception> {
        val dataOrException = DataOrException<List<MBook>, Boolean, Exception>()
        try {
            dataOrException.loading = true
            val snapshot = queryBook.get().await()
            val books = snapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(MBook::class.java)?.apply {
                    if (id.isNullOrEmpty()) {
                        id = documentSnapshot.id
                    }
                }
            }
            dataOrException.data = books
            dataOrException.loading = false
        } catch (exception: FirebaseFirestoreException) {
            dataOrException.e = exception
            dataOrException.loading = false
        } catch (e: Exception) {
            dataOrException.e = e
            dataOrException.loading = false
        }
        return dataOrException
    }
}
