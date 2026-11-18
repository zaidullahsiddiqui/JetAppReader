package com.example.jetappreader.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class MBook(
    @get:PropertyName("id") @set:PropertyName("id") var id: String? = null,
    @get:PropertyName("title") @set:PropertyName("title") var title: String? = null,
    @get:PropertyName("authors") @set:PropertyName("authors") var authors: String? = null,
    @get:PropertyName("notes") @set:PropertyName("notes") var notes: String? = null,
    @get:PropertyName("photo_url") @set:PropertyName("photo_url") var photoUrl: String? = null,
    @get:PropertyName("categories") @set:PropertyName("categories") var categories: String? = null,
    @get:PropertyName("published_date") @set:PropertyName("published_date") var publishedDate: String? = null,
    @get:PropertyName("rating") @set:PropertyName("rating") var rating: Double? = null,
    @get:PropertyName("description") @set:PropertyName("description") var description: String? = null,
    @get:PropertyName("page_count") @set:PropertyName("page_count") var pageCount: String? = null,
    @get:PropertyName("started_reading_at") @set:PropertyName("started_reading_at") var startedReadingAt: Timestamp? = null,
    @get:PropertyName("finished_reading_at") @set:PropertyName("finished_reading_at") var finishedReadingAt: Timestamp? = null,
    @get:PropertyName("user_id") @set:PropertyName("user_id") var userId: String? = null,
    @get:PropertyName("google_book_id") @set:PropertyName("google_book_id") var googleBookId: String? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "authors" to authors,
            "notes" to notes,
            "photo_url" to photoUrl,
            "categories" to categories,
            "published_date" to publishedDate,
            "rating" to rating,
            "description" to description,
            "page_count" to pageCount,
            "started_reading_at" to startedReadingAt,
            "finished_reading_at" to finishedReadingAt,
            "user_id" to userId,
            "google_book_id" to googleBookId
        )
    }
}
