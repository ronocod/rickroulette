package com.ronocod.rickroulette;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.ronocod.rickroulette.data.VideoContract;
import com.ronocod.rickroulette.sync.RickSyncAdapter;


/**
 * An activity representing a list of Videos. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link VideoDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link VideoListFragment} and the item details
 * (if present) is a {@link VideoDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link VideoListFragment.Listener} interface
 * to listen for item selections.
 */
public class VideoListActivity extends ActionBarActivity
        implements VideoListFragment.Listener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean useTwoPanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        if (findViewById(R.id.video_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            useTwoPanes = true;
        }


        getSupportActionBar().setElevation(0f);

        RickSyncAdapter.initializeSyncAdapter(this);
    }

    /**
     * Callback method from {@link VideoListFragment.Listener}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onVideoSelected(long id) {
        Uri uri = VideoContract.VideoSchema.buildUri(id);
        if (useTwoPanes) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.video_detail_container, VideoDetailFragment.create(uri))
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, VideoDetailActivity.class);
            detailIntent.putExtra(VideoDetailFragment.KEY_URI, uri);
            startActivity(detailIntent);
        }
    }
}
