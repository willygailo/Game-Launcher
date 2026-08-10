package com.gamebooster.app.ui.screens;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gamebooster.app.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

/**
 * SearchWebFragment — In-app browser for Shizuku documentation and web search.
 *
 * Quick-nav chips for:
 *  - Shizuku Setup Guide (https://shizuku.rikka.app/guide/setup/)
 *  - Wireless Debugging setup guide
 *  - Shizuku API reference
 *  - GitHub issues/releases
 *
 * Falls back to a DuckDuckGo web search for any user query.
 */
public class SearchWebFragment extends Fragment {

    private static final String SHIZUKU_SETUP_URL  = "https://shizuku.rikka.app/guide/setup/";
    private static final String WIRELESS_DEBUG_URL  = "https://shizuku.rikka.app/guide/setup/#use-wireless-debugging";
    private static final String SHIZUKU_API_URL     = "https://shizuku.rikka.app/api/";
    private static final String GITHUB_URL          = "https://github.com/RikkaApps/Shizuku/releases";
    private static final String SEARCH_BASE_URL     = "https://duckduckgo.com/?q=";

    private WebView mWebView;
    private TextInputEditText mSearchInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mWebView     = view.findViewById(R.id.wv_search);
        mSearchInput = view.findViewById(R.id.et_search_url);

        setupWebView();

        // Quick chips
        Chip chipSetup    = view.findViewById(R.id.chip_shizuku_setup);
        Chip chipWireless = view.findViewById(R.id.chip_shizuku_guide);
        Chip chipApi      = view.findViewById(R.id.chip_rikka_api);
        Chip chipGithub   = view.findViewById(R.id.chip_github);

        chipSetup.setOnClickListener(v    -> navigate(SHIZUKU_SETUP_URL));
        chipWireless.setOnClickListener(v -> navigate(WIRELESS_DEBUG_URL));
        chipApi.setOnClickListener(v      -> navigate(SHIZUKU_API_URL));
        chipGithub.setOnClickListener(v   -> navigate(GITHUB_URL));

        // Search / go button
        view.findViewById(R.id.btn_search_go).setOnClickListener(v -> submitSearch());
        mSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                submitSearch();
                return true;
            }
            return false;
        });

        // Default page
        navigate(SHIZUKU_SETUP_URL);
    }

    @Override
    public void onDestroyView() {
        if (mWebView != null) {
            mWebView.destroy();
        }
        super.onDestroyView();
    }

    // -----------------------------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------------------------

    private void setupWebView() {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mSearchInput != null) {
                    mSearchInput.setText(url);
                }
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient());
    }

    private void navigate(String url) {
        if (mWebView != null && url != null) {
            mWebView.loadUrl(url);
        }
    }

    private void submitSearch() {
        if (mSearchInput == null) return;
        String input = mSearchInput.getText() != null ? mSearchInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(input)) return;

        String url;
        if (input.startsWith("http://") || input.startsWith("https://")) {
            url = input;
        } else {
            url = SEARCH_BASE_URL + android.net.Uri.encode(input);
        }
        navigate(url);
    }
}
