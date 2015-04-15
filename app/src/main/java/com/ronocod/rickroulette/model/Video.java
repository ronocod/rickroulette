package com.ronocod.rickroulette.model;

/**
 * A dummy item representing a piece of content.
 */
public class Video {
    private final String youtubeId;
    private final String title;

    public Video(String youtubeId, String title) {
        this.youtubeId = youtubeId;
        this.title = title;
    }

    public String getYoutubeId() {
        return youtubeId;
    }

    public String getTitle() {
        return title;
    }
}
