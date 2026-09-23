package com.gamebooster.app.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gamebooster.app.R;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * WebBrowserActivity: Ultra-fast, hardware-accelerated in-app browser
 * optimized for web games (HTML5/WebGL), online guides, and search queries.
 */
public class WebBrowserActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_TITLE = "extra_title";

    private WebView webView;
    private ProgressBar progressBar;
    private EditText etUrl;

    public static void openUrl(Context context, String url, String title) {
        if (context == null) return;
        Intent intent = new Intent(context, WebBrowserActivity.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_TITLE, title);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);

        webView = findViewById(R.id.webview_browser);
        progressBar = findViewById(R.id.pb_browser_progress);
        etUrl = findViewById(R.id.et_browser_url);
        ImageButton btnBack = findViewById(R.id.btn_browser_back);
        Button btnGo = findViewById(R.id.btn_browser_go);
        ImageButton btnRefresh = findViewById(R.id.btn_browser_refresh);
        Button btnClose = findViewById(R.id.btn_browser_close);

        // Hardware Acceleration and Web Game Support
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressBar != null) {
                    if (newProgress < 100) {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(newProgress);
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (etUrl != null && !etUrl.hasFocus() && view != null && view.getUrl() != null) {
                    etUrl.setText(view.getUrl());
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (uri == null) return false;
                String scheme = uri.getScheme();
                if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                    return false;
                }
                // Handle market://, intent:// etc. externally
                try {
                    Intent externalIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(externalIntent);
                    return true;
                } catch (Exception e) {
                    return true;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (etUrl != null && !etUrl.hasFocus()) {
                    etUrl.setText(url);
                }
            }
        });

        btnBack.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
        });

        btnRefresh.setOnClickListener(v -> webView.reload());
        btnClose.setOnClickListener(v -> finish());

        Runnable performNavigation = () -> {
            String input = etUrl.getText().toString().trim();
            if (!input.isEmpty()) {
                loadUserQuery(input);
            }
        };

        btnGo.setOnClickListener(v -> performNavigation.run());
        etUrl.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH) {
                performNavigation.run();
                return true;
            }
            return false;
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        String initialUrl = getIntent().getStringExtra(EXTRA_URL);
        if (initialUrl == null || initialUrl.trim().isEmpty()) {
            initialUrl = "https://html.duckduckgo.com/";
        }
        loadUserQuery(initialUrl);
    }

    private void loadUserQuery(String query) {
        String url;
        if (query.startsWith("http://") || query.startsWith("https://")) {
            url = query;
        } else if (query.contains(".") && !query.contains(" ")) {
            url = "https://" + query;
        } else {
            try {
                url = "https://html.duckduckgo.com/html/?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                url = "https://html.duckduckgo.com/html/?q=" + Uri.encode(query);
            }
        }
        webView.loadUrl(url);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.destroy();
        }
        super.onDestroy();
    }
}
