package com.gamebooster.app.api;

import java.io.Serializable;

/**
 * Model representing an online game, web game, or web search item.
 */
public class OnlineGameSearchResult implements Serializable {
    private final String title;
    private final String description;
    private final String coverUrl;
    private final String webUrl;
    private final String category;
    private final float rating;
    private final boolean isWebGame;

    public OnlineGameSearchResult(String title, String description, String coverUrl,
                                  String webUrl, String category, float rating, boolean isWebGame) {
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.coverUrl = coverUrl != null ? coverUrl : "";
        this.webUrl = webUrl != null ? webUrl : "";
        this.category = category != null ? category : "Action";
        this.rating = rating;
        this.isWebGame = isWebGame;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getCategory() {
        return category;
    }

    public float getRating() {
        return rating;
    }

    public boolean isWebGame() {
        return isWebGame;
    }
}
