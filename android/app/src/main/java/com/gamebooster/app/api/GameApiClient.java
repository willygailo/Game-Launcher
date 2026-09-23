package com.gamebooster.app.api;

import android.net.Uri;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * High-performance Game Search & Web Gaming REST API Client.
 * Connects to online game APIs and search indices asynchronously.
 */
public class GameApiClient {

    private static final String TAG = "GameApiClient";
    private static final int TIMEOUT_MS = 8000;

    public interface SearchCallback {
        void onSuccess(List<OnlineGameSearchResult> results);
        void onError(String errorMessage);
    }

    /**
     * Searches online games and web games asynchronously.
     * Combines popular curated gaming databases and DuckDuckGo instant answer/web search.
     */
    public static void searchOnlineGames(String query, SearchCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            if (callback != null) {
                AppExecutors.getInstance().postToMainThread(() -> callback.onSuccess(getPopularFeaturedGames()));
            }
            return;
        }

        final String cleanQuery = query.trim();

        AppExecutors.getInstance().executeCommand(() -> {
            List<OnlineGameSearchResult> results = new ArrayList<>();
            try {
                // 1. Check curated list first for exact or fuzzy matches
                for (OnlineGameSearchResult curated : getPopularFeaturedGames()) {
                    if (curated.getTitle().toLowerCase().contains(cleanQuery.toLowerCase())
                            || curated.getCategory().toLowerCase().contains(cleanQuery.toLowerCase())
                            || curated.getDescription().toLowerCase().contains(cleanQuery.toLowerCase())) {
                        results.add(curated);
                    }
                }

                // 2. Query DuckDuckGo Instant Answer / Game Search API for real-time web results
                String encoded = URLEncoder.encode(cleanQuery + " game online", StandardCharsets.UTF_8.name());
                String endpoint = "https://api.duckduckgo.com/?q=" + encoded + "&format=json&no_html=1&skip_disambig=1";

                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; GameLauncher/19.1)");

                int code = conn.getResponseCode();
                if (code == 200) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                    conn.disconnect();

                    JSONObject json = new JSONObject(sb.toString());
                    String heading = json.optString("Heading", cleanQuery);
                    String abstractText = json.optString("AbstractText", "");
                    String abstractUrl = json.optString("AbstractURL", "");
                    String imageUrl = json.optString("Image", "");

                    if (!abstractText.isEmpty() && !abstractUrl.isEmpty()) {
                        results.add(new OnlineGameSearchResult(
                                heading,
                                abstractText,
                                imageUrl,
                                abstractUrl,
                                "Web Encyclopedia",
                                4.8f,
                                false
                        ));
                    }

                    JSONArray related = json.optJSONArray("RelatedTopics");
                    if (related != null) {
                        for (int i = 0; i < related.length() && results.size() < 15; i++) {
                            JSONObject topic = related.optJSONObject(i);
                            if (topic != null) {
                                String text = topic.optString("Text", "");
                                String firstUrl = topic.optString("FirstURL", "");
                                JSONObject iconObj = topic.optJSONObject("Icon");
                                String iconUrl = iconObj != null ? iconObj.optString("URL", "") : "";
                                if (!text.isEmpty() && !firstUrl.isEmpty()) {
                                    String title = text.length() > 40 ? text.substring(0, 40) + "..." : text;
                                    results.add(new OnlineGameSearchResult(
                                            title,
                                            text,
                                            iconUrl,
                                            firstUrl,
                                            "Online Search",
                                            4.5f,
                                            false
                                    ));
                                }
                            }
                        }
                    }
                }

                // 3. Fallback: Add direct Web Search navigation link if results are sparse
                if (results.isEmpty()) {
                    String directSearchUrl = "https://html.duckduckgo.com/html/?q=" + URLEncoder.encode(cleanQuery + " game", StandardCharsets.UTF_8.name());
                    results.add(new OnlineGameSearchResult(
                            "Search Web: " + cleanQuery,
                            "Explore web search results, guides, and online games for '" + cleanQuery + "'.",
                            "",
                            directSearchUrl,
                            "Web Search",
                            5.0f,
                            false
                    ));
                }

                final List<OnlineGameSearchResult> finalResults = results;
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (callback != null) callback.onSuccess(finalResults);
                });

            } catch (Throwable t) {
                Log.e(TAG, "Search error: " + t.getMessage(), t);
                // Fallback to direct web search link so the user is never blocked
                String directSearchUrl = "https://html.duckduckgo.com/html/?q=" + Uri.encode(cleanQuery + " game");
                List<OnlineGameSearchResult> fallback = Collections.singletonList(
                        new OnlineGameSearchResult(
                                "Search Web for \"" + cleanQuery + "\"",
                                "Tap to search the web for cheats, guides, downloads, and web play.",
                                "",
                                directSearchUrl,
                                "Web Search",
                                4.9f,
                                false
                        )
                );
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (callback != null) callback.onSuccess(fallback);
                });
            }
        });
    }

    /**
     * Curated featured web games and popular titles available to play or browse directly.
     */
    public static List<OnlineGameSearchResult> getPopularFeaturedGames() {
        List<OnlineGameSearchResult> list = new ArrayList<>();
        list.add(new OnlineGameSearchResult(
                "2048 Cyberpunk Edition",
                "Classic sliding puzzle game with cybernetic aesthetics and 120 FPS animations.",
                "https://play2048.co/favicon.ico",
                "https://play2048.co/",
                "Puzzle",
                4.9f,
                true
        ));
        list.add(new OnlineGameSearchResult(
                "Slither.io Mobile Web",
                "Real-time multiplayer snake combat game directly playable in web canvas.",
                "https://slither.io/favicon.ico",
                "https://slither.io/",
                "Multiplayer",
                4.8f,
                true
        ));
        list.add(new OnlineGameSearchResult(
                "Krunker.io FPS",
                "Fast-paced blocky first-person arena shooter with high-tickrate servers.",
                "https://krunker.io/favicon.ico",
                "https://krunker.io/",
                "Shooter",
                4.7f,
                true
        ));
        list.add(new OnlineGameSearchResult(
                "Mobile Legends Wiki & Guides",
                "Official hero builds, patch notes, damage formulas, and meta tiers.",
                "",
                "https://mobile-legends.fandom.com/wiki/Mobile_Legends:_Bang_Bang_Wiki",
                "Game Guide",
                4.9f,
                false
        ));
        list.add(new OnlineGameSearchResult(
                "PUBG Mobile Strategic DB",
                "Weapon recoils, bullet drop calculators, vehicle spawns, and map tactics.",
                "",
                "https://pubgmobile.fandom.com/wiki/PUBG_Mobile_Wiki",
                "Game Guide",
                4.8f,
                false
        ));
        list.add(new OnlineGameSearchResult(
                "Call of Duty: Mobile Intel",
                "Loadout metas, gunsmith codes, perk combos, and ranked battle royale spots.",
                "",
                "https://callofduty.fandom.com/wiki/Call_of_Duty:_Mobile",
                "Game Guide",
                4.9f,
                false
        ));
        return list;
    }
}
