package com.example.jetappreader.network

import com.example.jetappreader.model.Book
import com.example.jetappreader.model.Item
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Singleton

@Singleton
interface BookApi {

    @GET("volumes")
    suspend fun getAllBooks(
        @Query("q") query: String,
        @Query("key") apiKey: String? = null,
    ): Book

    @GET("volumes/{bookId}")
    suspend fun getBookInfo(
        @Path("bookId") bookId: String,
        @Query("key") apiKey: String? = null,
    ): Item
}


