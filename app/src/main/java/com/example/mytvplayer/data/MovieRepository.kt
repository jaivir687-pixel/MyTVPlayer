package com.example.mytvplayer.data

import com.example.mytvplayer.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class MovieRepository {
    private val client = OkHttpClient()
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    private val JSON_URL = "https://gist.githubusercontent.com/jaivir687-pixel/c4b386bfa0d56287c74e14942084dfa6/raw/movies.json"

    suspend fun getMovies(): List<Movie> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(JSON_URL)
                .addHeader("Cache-Control", "no-cache")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext getMockMovies("HTTP Error ${response.code}")
                
                val body = response.body?.string() ?: ""
                if (body.isBlank()) return@withContext getMockMovies("Empty Response")
                
                return@withContext try {
                    json.decodeFromString<List<Movie>>(body)
                } catch (e: Exception) {
                    android.util.Log.e("MovieRepository", "JSON Error: ${e.message}")
                    // If parsing fails, it's likely a missing ] or comma in the Gist
                    getMockMovies("JSON Syntax Error: ${e.localizedMessage}")
                }
            }
        } catch (e: Exception) {
            getMockMovies("Network Error: ${e.message}")
        }
    }

    private fun getMockMovies(errorDetail: String): List<Movie> {
        return listOf(
            Movie(
                id = "error",
                title = "JSON Syntax Error!",
                description = errorDetail,
                videoUrl = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
                thumbnailUrl = "https://via.placeholder.com/300x450/ff0000/ffffff?text=FIX+JSON",
                category = "Attention Required"
            )
        )
    }
}
