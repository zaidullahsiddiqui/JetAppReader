package com.example.jetappreader.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.jetappreader.components.FABContent
import com.example.jetappreader.components.ReaderAppBar
import com.example.jetappreader.data.DataOrException
import com.example.jetappreader.model.MBook
import com.example.jetappreader.utils.fixImageUrl
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

@Composable
fun ReaderHomeScreen(
    onSearchClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onBookClick: (String) -> Unit = {},
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val response by viewModel.data.collectAsState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.getAllBooksFromDatabase()
    }

    Scaffold(
        topBar = {
            ReaderAppBar(
                title = "A. Reader",
                showProfile = true,
                onProfileClick = onStatsClick,
                onLogoutClick = {
                    try {
                        FirebaseAuth.getInstance().signOut()
                    } catch (_: Exception) {
                    }
                    onLogoutClick()
                }
            )
        },
        floatingActionButton = {
            FABContent {
                onSearchClick()
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HomeContent(
                response = response,
                onBookClick = onBookClick,
                onStatsClick = onStatsClick
            )
        }
    }
}

@Composable
fun HomeContent(
    response: DataOrException<List<MBook>, Boolean, Exception>,
    onBookClick: (String) -> Unit = {},
    onStatsClick: () -> Unit = {}
) {
    val currentUser = try {
        FirebaseAuth.getInstance().currentUser
    } catch (_: Exception) {
        null
    }
    val currentUserId = currentUser?.uid ?: ""
    val currentUserName = if (!currentUser?.email.isNullOrEmpty()) {
        currentUser.email!!.split("@")[0]
    } else {
        "User"
    }

    val allBooks = response.data?.filter { it.userId == currentUserId } ?: emptyList()
    val readingList = allBooks.filter { it.finishedReadingAt == null }
    val finishedList = allBooks.filter { it.finishedReadingAt != null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        HomeHeaderCard(
            userName = currentUserName,
            readingCount = readingList.size,
            finishedCount = finishedList.size,
            onStatsClick = onStatsClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (response.loading == true) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (response.e != null) {
            Text(
                text = "Error fetching books: ${response.e?.message}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // Reading List Section
            Text(
                text = "Reading List (${readingList.size})",
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (readingList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No books found in reading list. Click + to add books!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                BookListRow(books = readingList, onBookClick = onBookClick)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Finished Reading Section
            Text(
                text = "Finished Reading (${finishedList.size})",
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (finishedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No finished books yet. Keep reading!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                BookListRow(books = finishedList, onBookClick = onBookClick)
            }
        }
    }
}

@Composable
fun BookListRow(
    books: List<MBook>,
    onBookClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp)
    ) {
        for (book in books) {
            BookItemCard(book = book, onBookClick = onBookClick)
        }
    }
}

@Composable
fun BookItemCard(
    book: MBook,
    onBookClick: (String) -> Unit
) {
    val context = LocalContext.current
    val imageUrl = fixImageUrl(book.photoUrl)

    Card(
        modifier = Modifier
            .width(150.dp)
            .height(245.dp)
            .padding(6.dp)
            .clickable { onBookClick(book.id ?: book.googleBookId ?: "") },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
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
                        contentDescription = "Default Book Icon",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = book.title ?: "No Title",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = book.authors ?: "Unknown",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val rating = book.rating ?: 0.0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", rating),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }

                    val statusText = when {
                        book.finishedReadingAt != null -> "Done"
                        book.startedReadingAt != null -> "Reading"
                        else -> "Want"
                    }

                    StatusBadge(statusText = statusText)
                }
            }
        }
    }
}

@Composable
fun StatusBadge(statusText: String) {
    val (bgColor, textColor) = when (statusText) {
        "Reading" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        "Done" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        else -> Color(0xFFFEF3C7) to Color(0xFFB45309)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color = textColor
        )
    }
}

@Composable
fun HomeHeaderCard(
    userName: String,
    readingCount: Int,
    finishedCount: Int,
    onStatsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clickable { onStatsClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back, $userName 👋",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "“Read. Learn. Grow.”",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ReaderHomeScreenPreview() {
    val sampleBooks = listOf(
        MBook(
            id = "1",
            userId = "",
            title = "The Basics of Programming in Java & JS",
            authors = "moaml mohmmed",
            rating = 3.0,
            startedReadingAt = com.google.firebase.Timestamp.now()
        ),
        MBook(
            id = "2",
            userId = "",
            title = "The Lord of the Rings: The Fellowship",
            authors = "J.R.R. Tolkien",
            rating = 4.0,
            startedReadingAt = com.google.firebase.Timestamp.now()
        )
    )
    com.example.jetappreader.ui.theme.JetAppReaderTheme {
        Surface {
            HomeContent(
                response = DataOrException(data = sampleBooks, loading = false, e = null)
            )
        }
    }
}
