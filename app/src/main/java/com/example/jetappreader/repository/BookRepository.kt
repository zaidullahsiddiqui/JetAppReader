package com.example.jetappreader.repository

import com.example.jetappreader.data.DataOrException
import com.example.jetappreader.data.Resource
import com.example.jetappreader.model.Item
import com.example.jetappreader.network.BookApi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class BookRepository @Inject constructor(private val api: BookApi) {

    suspend fun getBooks(searchQuery: String): DataOrException<List<Item>, Boolean, Exception> {
        val response = DataOrException<List<Item>, Boolean, Exception>()
        try {
            response.loading = true
            val items = api.getAllBooks(searchQuery).items
            response.data = items
            if (!response.data.isNullOrEmpty()) {
                response.loading = false
            }
        } catch (e: Exception) {
            response.e = e
            response.loading = false
        }
        return response
    }

    suspend fun getBookInfo(bookId: String): DataOrException<Item, Boolean, Exception> {
        val response = DataOrException<Item, Boolean, Exception>()
        try {
            response.loading = true
            response.data = api.getBookInfo(bookId)
            if (response.data != null) {
                response.loading = false
            }
        } catch (e: Exception) {
            response.e = e
            response.loading = false
        }
        return response
    }

    suspend fun getBooksResource(searchQuery: String): Resource<List<Item>> {
        return try {
            val items = api.getAllBooks(searchQuery).items
            Resource.Success(items ?: emptyList())
        } catch (e: HttpException) {
            Resource.Error(getHttpErrorMessage(e))
        } catch (_: IOException) {
            Resource.Error("Network connection error. Please check your internet connection.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unexpected error occurred fetching books")
        }
    }

    suspend fun getBookInfoResource(bookId: String): Resource<Item> {
        return try {
            val item = api.getBookInfo(bookId)
            Resource.Success(item)
        } catch (e: HttpException) {
            Resource.Error(getHttpErrorMessage(e))
        } catch (_: IOException) {
            Resource.Error("Network connection error. Please check your internet connection.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unexpected error occurred fetching book info")
        }
    }

    private fun getHttpErrorMessage(e: HttpException): String {
        return when (e.code()) {
            429 -> "Rate limit exceeded (HTTP 429). Google Books API received too many requests. Please wait a moment and try again."
            403 -> "API quota exceeded or access forbidden. Please try again later."
            404 -> "Book information not found."
            in 500..599 -> "Google Books server error (HTTP ${e.code()}). Please try again later."
            else -> "HTTP error ${e.code()}: ${e.message()}"
        }
    }
}

