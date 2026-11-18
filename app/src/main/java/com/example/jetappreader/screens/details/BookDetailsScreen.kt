package com.example.jetappreader.screens.details

import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jetappreader.components.ReaderAppBar
import com.example.jetappreader.data.Resource
import com.example.jetappreader.model.Item
import com.example.jetappreader.model.MBook
import com.example.jetappreader.utils.cleanDescription
import com.example.jetappreader.utils.fixImageUrl
import com.google.firebase.auth.FirebaseAuth

@Composable
fun BookDetailsScreen(
    bookId: String = "",
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = onBackClick,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val bookInfoState by viewModel.bookInfo.collectAsState()
    val isSaving by viewModel.saving.collectAsState()

    LaunchedEffect(bookId) {
        viewModel.getBookInfo(bookId)
    }

    Scaffold(
        topBar = {
            ReaderAppBar(
                title = "Book Details",
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
            when (val result = bookInfoState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Success -> {
                    val item = result.data
                    if (item == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Book details not found.")
                        }
                    } else {
                        BookDetailsContent(
                            item = item,
                            bookId = bookId,
                            isSaving = isSaving,
                            onSaveClick = { bookToSave ->
                                viewModel.saveBookToFirestore(
                                    book = bookToSave,
                                    onSuccess = {
                                        Toast.makeText(context, "Book saved to library!", Toast.LENGTH_SHORT).show()
                                        onSaveSuccess()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onCancelClick = onBackClick
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
                            text = result.message ?: "Failed to load book details.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookDetailsContent(
    item: Item,
    bookId: String,
    isSaving: Boolean,
    onSaveClick: (MBook) -> Unit,
    onCancelClick: () -> Unit
) {
    val context = LocalContext.current
    val volumeInfo = item.volumeInfo
    val imageUrl = fixImageUrl(
        volumeInfo?.imageLinks?.thumbnail ?: volumeInfo?.imageLinks?.smallThumbnail
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .width(160.dp)
                .height(220.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                        contentDescription = volumeInfo?.title ?: "Book cover",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Default cover",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = volumeInfo?.title ?: "No Title",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Authors: ${volumeInfo?.authors?.joinToString(", ") ?: "Unknown"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Published: ${volumeInfo?.publishedDate ?: "N/A"} | Pages: ${volumeInfo?.pageCount ?: "N/A"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = "Categories: ${volumeInfo?.categories?.joinToString(", ") ?: "N/A"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = cleanDescription(volumeInfo?.description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    val currentUser = try {
                        FirebaseAuth.getInstance().currentUser
                    } catch (_: Exception) {
                        null
                    }
                    val bookToSave = MBook(
                        title = volumeInfo?.title ?: "Unknown Title",
                        authors = volumeInfo?.authors?.joinToString(", ") ?: "Unknown Author",
                        description = volumeInfo?.description ?: "",
                        categories = volumeInfo?.categories?.joinToString(", ") ?: "",
                        notes = "",
                        photoUrl = imageUrl,
                        publishedDate = volumeInfo?.publishedDate ?: "",
                        pageCount = volumeInfo?.pageCount?.toString() ?: "0",
                        rating = 0.0,
                        googleBookId = bookId,
                        userId = currentUser?.uid ?: "",
                        startedReadingAt = null,
                        finishedReadingAt = null
                    )
                    onSaveClick(bookToSave)
                },
                enabled = !isSaving,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .padding(end = 4.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Save")
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onCancelClick,
                enabled = !isSaving,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(start = 4.dp)
            ) {
                Text(text = "Cancel")
            }
        }
    }
}
