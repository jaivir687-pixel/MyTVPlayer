package com.example.mytvplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.mytvplayer.model.Movie
import com.example.mytvplayer.ui.theme.AccentBlue
import com.example.mytvplayer.ui.theme.EpisodeTileBackground
import com.example.mytvplayer.ui.theme.RatingYellow

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroSection(
    movie: Movie,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Background Image
        AsyncImage(
            model = movie.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 1000f
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(48.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = movie.title.uppercase(),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Text(text = "2024", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                Text(text = "Action / Fantasy", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    colors = SurfaceDefaults.colors(containerColor = Color.DarkGray.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = " A ",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 10.sp
                    )
                }
                Text(text = "2h 18m", color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
            }

            Text(
                text = movie.description.ifBlank { "A fearless warrior rises against the forces of darkness to protect a kingdom on the brink of destruction." },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.LightGray,
                modifier = Modifier.widthIn(max = 500.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.colors(
                        containerColor = EpisodeTileBackground,
                        contentColor = Color.White,
                        focusedContainerColor = EpisodeTileBackground.copy(alpha = 0.8f),
                        focusedContentColor = RatingYellow
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Now")
                }

                OutlinedButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.colors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        focusedContainerColor = EpisodeTileBackground.copy(alpha = 0.8f),
                        focusedContentColor = RatingYellow
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("My List")
                }
            }
        }
    }
}
