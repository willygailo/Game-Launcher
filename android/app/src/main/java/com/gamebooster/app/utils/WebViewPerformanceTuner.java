package com.gamebooster.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.gamebooster.app.core.AppExecutors;

/**
 * High-Performance WebView Optimization Engine for In-Game WebViews, HTML5/WebGL canvases,
 * Login Portals, and Overlay Windows.
 */
public final class WebViewPerformanceTuner {

    private WebViewPerformanceTuner() {
    }

    /**
     * Programmatically optimizes a WebView instance for maximum FPS, hardware-accelerated
     * rendering, smooth scrolling, and lowest CPU/GPU latency.
     */
    @SuppressLint("SetJavaScriptEnabled")
    public static void optimize(WebView webView) {
        if (webView == null) return;

        // 1. Hardware Layer Acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setDrawingCacheEnabled(false);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // 2. WebSettings High-Throughput Configuration
        WebSettings settings = webView.getSettings();
        if (settings != null) {
            settings.setJavaScriptEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            
            // Texture & Caching Optimizations
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);
            settings.setSupportZoom(false);
            settings.setBuiltInZoomControls(false);
            settings.setDisplayZoomControls(false);

            // Deprecated RenderPriority fallback for older APIs
            try {
                settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
            } catch (Throwable ignored) {
            }

            // Android 5.0+ Offscreen Pre-Rasterization (Eliminates checkerboarding & frame drops)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    settings.setOffscreenPreRaster(true);
                    CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
                } catch (Throwable ignored) {
                }
            }

            // Android 8.0+ Safe Browsing Control (Reduces network latency for verified game assets)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    settings.setSafeBrowsingEnabled(false);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    /**
     * Pre-warms WebView process in the background to eliminate cold startup lag.
     */
    public static void prewarmWebViewProcess(Context context) {
        if (context == null) return;
        AppExecutors.getInstance().postToMainThread(() -> {
            try {
                WebView dummy = new WebView(context.getApplicationContext());
                optimize(dummy);
                dummy.destroy();
            } catch (Throwable ignored) {
            }
        });
    }
}
