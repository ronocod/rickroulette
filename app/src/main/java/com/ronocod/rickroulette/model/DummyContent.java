package com.ronocod.rickroulette.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    private static List<Video> ITEMS = Arrays.asList(
            new Video("1", "Item 1"),
            new Video("2", "Item 2"),
            new Video("3", "Item 3")
    );


    /**
     * A map of sample (dummy) items, by ID.
     */
    private static Map<String, Video> ITEM_MAP = new HashMap<>();

    static {
        for (Video item : ITEMS) {
            ITEM_MAP.put(item.getYoutubeId(), item);
        }
    }

    public static Video getItem(String key) {
        return DummyContent.ITEM_MAP.get(key);
    }

}
