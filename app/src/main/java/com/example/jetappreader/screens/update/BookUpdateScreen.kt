package com.example.jetappreader.screens.update

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jetappreader.components.InputField
import com.example.jetappreader.components.ReaderAppBar
import com.example.jetappreader.data.Resource
import com.example.jetappreader.model.MBook
import com.example.jetappreader.utils.fixImageUrl
import com.example.jetappreader.utils.formatDate
import com.google.firebase.Timestamp

enum class ReadingStatus {
    WANT_TO_READ,
    CURRENTLY_READING,
    FINISHED
}

@Composable
fun BookUpdateScreen(
    bookId: String = "",
    onBackClick: () -> Unit = {},
    viewModel: BookUpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val bookState by viewModel.bookState.collectAsState()
    val isUpdating by viewModel.updating.collectAsState()

    LaunchedEffect(bookId) {
        viewModel.getBook(bookId)
    }

    Scaffold(
        topBar = {
            ReaderAppBar(
                title = "Update Book",
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                showProfile = false,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val result = bookState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Success -> {
                    val book = result.data
                    if (book == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Book details not found.")
                        }
                    } else {
                        BookUpdateContent(
                            book = book,
                            isUpdating = isUpdating,
                            onUpdateClick = { docId, updateMap ->
                                viewModel.updateBook(
                                    docId = docId,
                                    updateData = updateMap,
                                    onSuccess = {
                                        Toast.makeText(context, "Book updated successfully!", Toast.LENGTH_SHORT).show()
                                        onBackClick()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onDeleteClick = { docId ->
                                viewModel.deleteBook(
                                    docId = docId,
                                    onSuccess = {
                                        Toast.makeText(context, "Book deleted!", Toast.LENGTH_SHORT).show()
                                        onBackClick()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )
                    }
                }

                is Resource.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = result.message ?: "Failed to load book.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookUpdateContent(
    book: MBook,
    isUpdating: Boolean,
    onUpdateClick: (String, Map<String, Any?>) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val context = LocalContext.current
    val imageUrl = fixImageUrl(book.photoUrl)

    val notesState = rememberSaveable { mutableStateOf(book.notes ?: "") }
    var ratingState by rememberSaveable { mutableDoubleStateOf(book.rating ?: 0.0) }

    val initialStatus = when {
        book.finishedReadingAt != null -> ReadingStatus.FINISHED
        book.startedReadingAt != null -> ReadingStatus.CURRENTLY_READING
        else -> ReadingStatus.WANT_TO_READ
    }

    var statusState by remember { mutableStateOf(initialStatus) }
    var startedTimestamp by remember { mutableStateOf(book.startedReadingAt) }
    var finishedTimestamp by remember { mutableStateOf(book.finishedReadingAt) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .width(100.dp)
                    .height(140.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = book.title ?: "Book cover",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Default cover",
                            modifier = Modifier.size(50.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = book.title ?: "No Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Author: ${book.authors ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Published: ${book.publishedDate ?: "N/A"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Rating Section
        Text(
            text = "Rating",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        InteractiveRatingBar(
            rating = ratingState.toInt(),
            onRatingSelected = { selectedRating ->
                ratingState = selectedRating.toDouble()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Reading Status Section
        Text(
            text = "Reading Status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilterChip(
                selected = statusState == ReadingStatus.WANT_TO_READ,
                onClick = {
                    statusState = ReadingStatus.WANT_TO_READ
                    startedTimestamp = null
                    finishedTimestamp = null
                },
                label = { Text("Want to Read") }
            )

            FilterChip(
                selected = statusState == ReadingStatus.CURRENTLY_READING,
                onClick = {
                    statusState = ReadingStatus.CURRENTLY_READING
                    if (startedTimestamp == null) {
                        startedTimestamp = Timestamp.now()
                    }
                    finishedTimestamp = null
                },
                label = { Text("Reading") }
            )

            FilterChip(
                selected = statusState == ReadingStatus.FINISHED,
                onClick = {
                    statusState = ReadingStatus.FINISHED
                    if (startedTimestamp == null) {
                        startedTimestamp = Timestamp.now()
                    }
                    if (finishedTimestamp == null) {
                        finishedTimestamp = Timestamp.now()
                    }
                },
                label = { Text("Finished") }
            )
        }

        // Display dates if present
        if (startedTimestamp != null || finishedTimestamp != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    val startTs = startedTimestamp
                    if (startTs != null) {
                        Text(
                            text = "Started Reading: ${formatDate(startTs)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    val finishTs = finishedTimestamp
                    if (finishTs != null) {
                        Text(
                            text = "Finished Reading: ${formatDate(finishTs)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notes Input
        InputField(
            valueState = notesState,
            labelId = "Notes & Thoughts",
            enabled = !isUpdating,
            isSingleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons: Update and Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    val docId = book.id ?: ""
                    val updateMap = mutableMapOf<String, Any?>(
                        "notes" to notesState.value,
                        "rating" to ratingState,
                        "started_reading_at" to startedTimestamp,
                        "finished_reading_at" to finishedTimestamp
                    )
                    onUpdateClick(docId, updateMap)
                },
                enabled = !isUpdating,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .padding(end = 4.dp)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Update")
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { showDeleteDialog = true },
                enabled = !isUpdating,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(start = 4.dp)
            ) {
                Text(text = "Delete", color = MaterialTheme.colorScheme.onError)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Delete Book") },
            text = { Text(text = "Are you sure you want to delete '${book.title}' from your library?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick(book.id ?: "")
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InteractiveRatingBar(
    rating: Int,
    onRatingSelected: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star $i",
                tint = if (i <= rating) Color(0xFFFFB800) else Color.Gray,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onRatingSelected(i) }
                    .padding(2.dp)
            )
        }
    }
}
