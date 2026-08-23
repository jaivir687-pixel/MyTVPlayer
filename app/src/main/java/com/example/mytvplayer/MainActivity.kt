@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package com.example.mytvplayer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mytvplayer.data.MovieRepository
import com.example.mytvplayer.model.Episode
import com.example.mytvplayer.model.Movie
import com.example.mytvplayer.ui.components.HeroSection
import com.example.mytvplayer.ui.components.SideBar
import com.example.mytvplayer.ui.components.VideoPlayer
import com.example.mytvplayer.ui.theme.EpisodeTileBackground
import com.example.mytvplayer.ui.theme.MyTVPlayerTheme
import com.example.mytvplayer.BuildConfig
import com.example.mytvplayer.ui.theme.RatingYellow
import com.example.mytvplayer.util.UpdateInfo
import com.example.mytvplayer.util.UpdateManager
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTVPlayerTheme {
                Surface(modifier = Modifier.fillMaxSize(), shape = RectangleShape) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updateManager = remember { UpdateManager(context) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    var selectedMovie by remember { mutableStateOf<Movie?>(null) }
    var selectedEpisode by remember { mutableStateOf<Episode?>(null) }

    LaunchedEffect(Unit) {
        val info = updateManager.checkForUpdates("https://raw.githubusercontent.com/jaivir687-pixel/MyTVPlayer/main/update.json")
        if (info != null && info.versionCode > BuildConfig.VERSION_CODE) {
            updateInfo = info
            showUpdateDialog = true
        }
    }

    if (showUpdateDialog && updateInfo != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showUpdateDialog = false }) {
            Surface(
                modifier = Modifier.width(400.dp).wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = SurfaceDefaults.colors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text(
                        "New Update Available!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Version: ${updateInfo!!.versionName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        updateInfo!!.releaseNotes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showUpdateDialog = false }) {
                            Text("Later")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                showUpdateDialog = false
                                updateManager.downloadAndInstall(updateInfo!!.apkUrl, "MyTVPlayer_${updateInfo!!.versionName}.apk")
                                Toast.makeText(context, "Download started...", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.colors(containerColor = RatingYellow, contentColor = Color.Black)
                        ) {
                            Text("Update Now")
                        }
                    }
                }
            }
        }
    }

    if (selectedEpisode != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            VideoPlayer(videoUrl = selectedEpisode!!.videoUrl)
            BackHandler { selectedEpisode = null }
        }
    } else if (selectedMovie != null) {
        if (selectedMovie!!.isSeries) {
            SeriesDetailScreen(
                series = selectedMovie!!,
                onEpisodeClick = { selectedEpisode = it },
                onBack = { selectedMovie = null }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                VideoPlayer(videoUrl = selectedMovie!!.videoUrl)
                BackHandler { selectedMovie = null }
            }
        }
    } else {
        MainScreen(onMovieClick = { selectedMovie = it })
    }
}

@Composable
fun MainScreen(onMovieClick: (Movie) -> Unit) {
    val repository = remember { MovieRepository() }
    var allMovies by remember { mutableStateOf<List<Movie>?>(null) }
    var selectedNavItem by remember { mutableStateOf("Home") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        allMovies = repository.getMovies()
    }

    Row(modifier = Modifier.fillMaxSize()) {
        SideBar(onNavItemClick = { 
            selectedNavItem = it 
            if (it != "Search") {
                isSearchActive = false
                searchQuery = ""
            }
        })

        if (allMovies == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading Content...", style = MaterialTheme.typography.headlineMedium)
            }
        } else {
            // Filter movies based on selection and search query
            val filteredMovies = remember(selectedNavItem, allMovies, searchQuery) {
                var list = allMovies!!
                
                // First filter by Category/Sidebar selection
                list = when (selectedNavItem) {
                    "Movies" -> list.filter { !it.isSeries }
                    "TV Shows" -> list.filter { it.isSeries }
                    else -> list
                }
                
                // Then filter by Search Query
                if (searchQuery.isNotEmpty()) {
                    list = list.filter { 
                        it.title.contains(searchQuery, ignoreCase = true) || 
                        it.category.contains(searchQuery, ignoreCase = true)
                    }
                }
                list
            }

            if (selectedNavItem == "Settings") {
                SettingsScreen()
            } else {
                if (isSearchActive) {
                    BackHandler { 
                        isSearchActive = false
                        searchQuery = ""
                    }
                }

                val featuredMovie = remember(filteredMovies) { filteredMovies.firstOrNull() }
                val groupedMovies = remember(filteredMovies) { filteredMovies.groupBy { it.category } }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(40.dp),
                    contentPadding = PaddingValues(bottom = 48.dp, top = 24.dp)
                ) {
                    // Top Search Bar
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Search Results" else if (selectedNavItem == "Home") "Discover" else selectedNavItem,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            // SEARCH BAR
                            Surface(
                                onClick = { 
                                    isSearchActive = true
                                },
                                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(24.dp)),
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.2f)
                                ),
                                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
                                modifier = Modifier
                                    .width(400.dp)
                                    .height(48.dp)
                                    .focusRequester(searchFocusRequester)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = RatingYellow)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(searchFocusRequester),
                                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                                        cursorBrush = SolidColor(RatingYellow),
                                        singleLine = true,
                                        decorationBox = { innerTextField ->
                                            if (searchQuery.isEmpty()) {
                                                Text("Search movies, shows...", color = Color.Gray, fontSize = 16.sp)
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // No Results Placeholder
                    if (filteredMovies.isEmpty() && searchQuery.isNotEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("No results found for '$searchQuery'", color = Color.Gray)
                            }
                        }
                    }

                    // Hero Section - Hide if searching
                    if (featuredMovie != null && searchQuery.isEmpty()) {
                        item {
                            HeroSection(
                                movie = featuredMovie,
                                onPlayClick = { onMovieClick(featuredMovie) }
                            )
                        }
                    }

                    // Categorized Rows
                    groupedMovies.forEach { (category, movieList) ->
                        item {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "See All >",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.Gray
                                    )
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    items(
                                        items = movieList,
                                        key = { movie: Movie -> movie.id }
                                    ) { movie: Movie ->
                                        MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            SettingItem(
                title = "Video Quality",
                subtitle = "Current: Auto (High Priority)",
                icon = Icons.Default.HighQuality
            ) {
                Toast.makeText(context, "Quality set to Auto", Toast.LENGTH_SHORT).show()
            }
        }

        item {
            SettingItem(
                title = "Clear Cache",
                subtitle = "Free up 124 MB of space",
                icon = Icons.Default.Delete
            ) {
                Toast.makeText(context, "Cache Cleared Successfully", Toast.LENGTH_SHORT).show()
            }
        }

        item {
            SettingItem(
                title = "Account Details",
                subtitle = "VIP Member - Expires 2027",
                icon = Icons.Default.Person
            ) {
                // Future profile settings
            }
        }

        item {
            SettingItem(
                title = "App Version",
                subtitle = "v1.0.4 (Stable Build)",
                icon = Icons.Default.Info
            ) {
                Toast.makeText(context, "You are on the latest version", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.05f),
            focusedContainerColor = Color.White.copy(alpha = 0.15f)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RatingYellow,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    val context = LocalContext.current

    // ⭐ 3. Fast Downsampled Image Request
    val imageRequest = remember(movie.thumbnailUrl) {
        ImageRequest.Builder(context)
            .data(movie.thumbnailUrl)
            .size(320, 480)
            .crossfade(150)
            .build()
    }

    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp).height(240.dp),
        shape = CardDefaults.shape(RoundedCornerShape(12.dp)),
        scale = CardDefaults.scale(focusedScale = 1.08f),
        colors = CardDefaults.colors(
            containerColor = EpisodeTileBackground,
            contentColor = Color.White,
            focusedContainerColor = EpisodeTileBackground.copy(alpha = 0.8f),
            focusedContentColor = RatingYellow
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Rating Badge
            Surface(
                shape = RoundedCornerShape(bottomEnd = 8.dp),
                colors = SurfaceDefaults.colors(containerColor = Color.Black.copy(alpha = 0.7f)),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = RatingYellow,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "8.5",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom Info Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(0.9f)),
                            startY = 300f
                        )
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = movie.title.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = LocalContentColor.current,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold
                    )
                    if (movie.isSeries) {
                        Text(
                            text = "SERIES",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (LocalContentColor.current == Color.White) Color.LightGray else RatingYellow.copy(alpha = 0.7f),
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeriesDetailScreen(series: Movie, onEpisodeClick: (Episode) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }
    Column(modifier = Modifier.fillMaxSize().padding(48.dp)) {
        Text(
            text = series.title.uppercase(),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = series.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Episodes", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(
                items = series.episodes,
                key = { episode: Episode -> episode.id }
            ) { episode: Episode ->
                Button(
                    onClick = { onEpisodeClick(episode) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.colors(
                        containerColor = EpisodeTileBackground,
                        contentColor = Color.White,
                        focusedContainerColor = EpisodeTileBackground.copy(alpha = 0.8f),
                        focusedContentColor = RatingYellow
                    )
                ) {
                    Text(
                        text = episode.title.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}