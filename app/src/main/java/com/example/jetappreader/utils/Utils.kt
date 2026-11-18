package com.example.jetappreader.utils

import com.google.firebase.Timestamp
import java.text.DateFormat

fun formatDate(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val date = timestamp.toDate()
    return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)
}

fun cleanDescription(description: String?): String {
    if (description.isNullOrEmpty()) return "No description available."
    return description
        .replace(Regex("<[^>]*>"), "")
        .replace("&quot;", "\"")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .trim()
}

fun fixImageUrl(url: String?): String {
    if (url.isNullOrEmpty()) return ""
    return if (url.startsWith("http://")) {
        url.replace("http://", "https://")
    } else {
        url
    }
}
