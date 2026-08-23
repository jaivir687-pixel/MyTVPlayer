package com.example.mytvplayer.ui.components

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream

@Composable
fun WebViewPlayer(
    url: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    displayZoomControls = false
                    builtInZoomControls = false
                }

                webViewClient = object : WebViewClient() {
                    // Block common ad domains
                    private val adDomains = listOf(
                        "doubleclick.net",
                        "google-analytics.com",
                        "popads.net",
                        "popcash.net",
                        "adservice.google",
                        "mxdcontent.net/ads"
                    )

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val requestUrl = request?.url?.toString() ?: ""
                        for (domain in adDomains) {
                            if (requestUrl.contains(domain)) {
                                // Return empty response for ad domains
                                return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
                            }
                        }
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        // Inject JavaScript to hide common ad overlays and popups
                        view?.evaluateJavascript(
                            """
                            (function() {
                                var ads = document.querySelectorAll('[id*="ad"], [class*="ad"], .popunder, .popup, #overlay');
                                for (var i = 0; i < ads.length; i++) {
                                    ads[i].style.display = 'none';
                                }
                                // Auto-click play button if possible or remove overlays
                                document.body.style.overflow = 'auto';
                            })();
                            """.trimIndent(),
                            null
                        )
                    }
                }
                
                loadUrl(url)
            }
        }
    )
}
